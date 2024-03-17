// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.audio.formats;

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Info;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Decompresses an Ogg file.
 * <br><br>
 * How to use:<br>
 * 1. Create OggInputStream passing in the input stream of the packed ogg file<br>
 * 2. Fetch format and sampling rate using getFormat() and getRate(). Use it to
 * initialize the sound player.<br>
 * 3. Read the PCM data using one of the read functions, and feed it to your player.
 * <br><br>
 * OggInputStream provides a read(ByteBuffer, int, int) that can be used to read
 * data directly into a native buffer.
 */
public class OggReader extends FilterInputStream {
    private static final Logger logger = LoggerFactory.getLogger(OggReader.class);

    /**
     * The mono 16 bit format
     */
    private static final int FORMAT_MONO16 = 1;

    /**
     * The stereo 16 bit format
     */
    private static final int FORMAT_STEREO16 = 2;

    /// Conversion buffer size
    private static int convsize = 4096 * 2;

    // Conversion buffer
    private static final byte[] CONVBUFFER = new byte[convsize];

    // temp vars
    private final float[][][] pcm = new float[1][][];
    private int[] index;

    // end of stream
    private boolean eos;

    // sync and verify incoming physical bitstream
    private final SyncState syncState = new SyncState();

    // take physical pages, weld into a logical stream of packets
    private final StreamState streamState = new StreamState();

    // one Ogg bitstream page.  Vorbis packets are inside
    private final Page page = new Page();

    // one raw packet of data for decode
    private final Packet packet = new Packet();

    // struct that stores all the static vorbis bitstream settings
    private final Info info = new Info();

    // struct that stores all the bitstream user comments
    private final Comment comment = new Comment();

    // central working state for the packet->PCM decoder
    private final DspState dspState = new DspState();

    // local working space for packet->PCM decode
    private final Block block = new Block(dspState);

    // where we are in the convbuffer
    private int convbufferOff;

    // bytes ready in convbuffer.
    private int convbufferSize;

    // a dummy used by read() to read 1 byte.
    private final byte[] readDummy = new byte[1];

    /**
     * Creates an OggInputStream that decompressed the specified ogg file.
     */
    public OggReader(InputStream input) {
        super(input);
        try {
            initVorbis();
            index = new int[info.channels];
        } catch (Exception e) {
            logger.error("Failed to read ogg file", e);
            eos = true;
        }
    }

    /**
     * Gets the format of the ogg file. Is either FORMAT_MONO16 or FORMAT_STEREO16
     */
    public int getChannels() {
        return info.channels;
    }

    /**
     * Gets the rate of the pcm audio.
     */
    public int getRate() {
        return info.rate;
    }

    /**
     * Reads the next byte of data from this input stream. The value byte is
     * returned as an int in the range 0 to 255. If no byte is available because
     * the end of the stream has been reached, the value -1 is returned. This
     * method blocks until input data is available, the end of the stream is
     * detected, or an exception is thrown.
     *
     * @return the next byte of data, or -1 if the end of the stream is reached.
     */
    @Override
    public int read() throws IOException {
        int retVal = read(readDummy, 0, 1);
        return (retVal == -1 ? -1 : readDummy[0]);
    }

    /**
     * Reads up to len bytes of data from the input stream into an array of bytes.
     *
     * @param b   the buffer into which the data is read.
     * @param off the start offset of the data.
     * @param len the maximum number of bytes read.
     * @return the total number of bytes read into the buffer, or -1 if there is
     *         no more data because the end of the stream has been reached.
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (eos) {
            return -1;
        }
        int bytesRead = 0;
        int bytesRemaining = len;
        int offset = off;
        while (!eos && (bytesRemaining > 0)) {
            fillConvbuffer();
            if (!eos) {
                int bytesToCopy = Math.min(bytesRemaining, convbufferSize - convbufferOff);
                System.arraycopy(CONVBUFFER, convbufferOff, b, offset, bytesToCopy);
                convbufferOff += bytesToCopy;
                bytesRead += bytesToCopy;
                bytesRemaining -= bytesToCopy;
                offset += bytesToCopy;
            }
        }
        return bytesRead;
    }

    /**
     * Reads up to len bytes of data from the input stream into a ByteBuffer.
     *
     * @param b   the buffer into which the data is read.
     * @param off the start offset of the data.
     * @param len the maximum number of bytes read.
     * @return the total number of bytes read into the buffer, or -1 if there is
     *         no more data because the end of the stream has been reached.
     */
    public int read(ByteBuffer b, int off, int len) throws IOException {
        if (eos) {
            return -1;
        }
        b.position(off);
        int bytesRead = 0;
        int bytesRemaining = len;
        while (!eos && (bytesRemaining > 0)) {
            fillConvbuffer();
            if (!eos) {
                int bytesToCopy = Math.min(bytesRemaining, convbufferSize - convbufferOff);
                b.put(CONVBUFFER, convbufferOff, bytesToCopy);
                convbufferOff += bytesToCopy;
                bytesRead += bytesToCopy;
                bytesRemaining -= bytesToCopy;
            }
        }
        return bytesRead;
    }

    /**
     * Helper function. Decodes a packet to the convbuffer if it is empty.
     * Updates convbufferSize, convbufferOff, and eos.
     */
    private void fillConvbuffer() throws IOException {
        if (convbufferOff >= convbufferSize) {
            convbufferSize = lazyDecodePacket();
            convbufferOff = 0;
            if (convbufferSize == -1) {
                eos = true;
            }
        }
    }

    /**
     * Returns 0 after EOF is reached, otherwise always return 1.
     * <br><br>
     * Programs should not count on this method to return the actual number of
     * bytes that could be read without blocking.
     *
     * @return 1 before EOF and 0 after EOF is reached.
     */
    @Override
    public int available() throws IOException {
        return (eos ? 0 : 1);
    }

    /**
     * OggInputStream does not support mark and reset. This function does nothing.
     */
    @Override
    public synchronized void reset() throws IOException {
    }

    /**
     * OggInputStream does not support mark and reset.
     *
     * @return false.
     */
    @Override
    public boolean markSupported() {
        return false;
    }

    /**
     * Skips over and discards n bytes of data from the input stream. The skip
     * method may, for a variety of reasons, end up skipping over some smaller
     * number of bytes, possibly 0. The actual number of bytes skipped is returned.
     *
     * @param n the number of bytes to be skipped.
     * @return the actual number of bytes skipped.
     */
    @Override
    public long skip(long n) throws IOException {
        int bytesRead = 0;
        while (bytesRead < n) {
            int res = read();
            if (res == -1) {
                break;
            }
            bytesRead++;
        }
        return bytesRead;
    }

    /**
     * Initalizes the vorbis stream. Reads the stream until info and comment are read.
     */
    private void initVorbis() throws IOException {
        // Now we can read pages
        syncState.init();

        // grab some data at the head of the stream.  We want the first page
        // (which is guaranteed to be small and only contain the Vorbis
        // stream initial header) We need the first page to get the stream
        // serialno.

        // submit a 4k block to libvorbis' Ogg layer
        int bufferIndex = syncState.buffer(4096);
        byte[] buffer = syncState.data;
        int bytes = in.read(buffer, bufferIndex, 4096);
        syncState.wrote(bytes);

        // Get the first page.
        if (syncState.pageout(page) != 1) {
            // have we simply run out of data?  If so, we're done.
            if (bytes < 4096) {
                return; //break;
            }
            // error case.  Must not be Vorbis data
            throw new IOException("Input does not appear to be an Ogg bitstream.");
        }

        // Get the serial number and set up the rest of decode.
        // serialno first; use it to set up a logical stream
        streamState.init(page.serialno());

        // extract the initial header from the first page and verify that the
        // Ogg bitstream is in fact Vorbis data

        // I handle the initial header first instead of just having the code
        // read all three Vorbis headers at once because reading the initial
        // header is an easy way to identify a Vorbis bitstream and it's
        // useful to see that functionality seperated out.

        info.init();
        comment.init();
        if (streamState.pagein(page) < 0) {
            // error; stream version mismatch perhaps
            throw new IOException("Error reading first page of Ogg bitstream data.");
        }
        if (streamState.packetout(packet) != 1) {
            // no page? must not be vorbis
            throw new IOException("Error reading initial header packet.");
        }
        if (info.synthesis_headerin(comment, packet) < 0) {
            // error case; not a vorbis header
            throw new IOException("This Ogg bitstream does not contain Vorbis audio data.");
        }

        // At this point, we're sure we're Vorbis.  We've set up the logical
        // (Ogg) bitstream decoder.  Get the comment and codebook headers and
        // set up the Vorbis decoder

        // The next two packets in order are the comment and codebook headers.
        // They're likely large and may span multiple pages.  Thus we read
        // and submit data until we get our two packets, watching that no
        // pages are missing.  If a page is missing, error out; losing a
        // header page is the only place where missing data is fatal.


        int i = 0;
        while (i < 2) {
            while (i < 2) {
                int result = syncState.pageout(page);
                if (result == 0) {
                    break; // Need more data
                }
                // Don't complain about missing or corrupt data yet.  We'll
                // catch it at the packet output phase

                if (result == 1) {
                    streamState.pagein(page); // we can ignore any errors here
                    // as they'll also become apparent
                    // at packet out
                    while (i < 2) {
                        result = streamState.packetout(packet);
                        if (result == 0) {
                            break;
                        }
                        if (result == -1) {
                            // Uh oh; data at some point was corrupted or missing!
                            // We can't tolerate that in a header.  Die.
                            throw new IOException("Corrupt secondary header. Exiting.");
                        }
                        info.synthesis_headerin(comment, packet);
                        i++;
                    }
                }
            }

            // no harm in not checking before adding more
            bufferIndex = syncState.buffer(4096);
            buffer = syncState.data;
            bytes = in.read(buffer, bufferIndex, 4096);

            // NOTE: This is a bugfix. read will return -1 which will mess up syncState.
            if (bytes < 0) {
                bytes = 0;
            }
            if (bytes == 0 && i < 2) {
                throw new IOException("End of file before finding all Vorbis headers!");
            }
            syncState.wrote(bytes);
        }

        convsize = 4096 / info.channels;

        // OK, got and parsed all three headers. Initialize the Vorbis
        //  packet->PCM decoder.
        dspState.synthesis_init(info); // central decode state
        block.init(dspState); // local state for most of the decode
        // so multiple block decodes can
        // proceed in parallel.  We could init
        // multiple vorbis_block structures
        // for vd here
    }

    /**
     * Decodes a packet.
     */
    private int decodePacket() {
        // check the endianes of the computer.
        final boolean bigEndian = ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN);

        if (block.synthesis(packet) == 0) {
            dspState.synthesis_blockin(block);
        }

        // **pcm is a multichannel float vector.  In stereo, for
        // example, pcm[0] is left, and pcm[1] is right.  samples is
        // the size of each channel.  Convert the float values
        // (-1.<=range<=1.) to whatever PCM format and write it out
        int convOff = 0;
        int samples;
        while ((samples = dspState.synthesis_pcmout(pcm, index)) > 0) {
            float[][] localPcm = this.pcm[0];
            int bout = (Math.min(samples, convsize));

            // convert floats to 16 bit signed ints (host order) and interleave
            for (int i = 0; i < info.channels; i++) {
                int ptr = (i << 1) + convOff;


                int mono = index[i];

                for (int j = 0; j < bout; j++) {
                    int val = (int) (localPcm[i][mono + j] * 32767);

                    // might as well guard against clipping
                    val = Math.max(-32768, Math.min(32767, val));
                    val |= (val < 0 ? 0x8000 : 0);

                    CONVBUFFER[ptr + 0] = (byte) (bigEndian ? val >>> 8 : val);
                    CONVBUFFER[ptr + 1] = (byte) (bigEndian ? val : val >>> 8);

                    ptr += (info.channels) << 1;
                }
            }

            convOff += 2 * info.channels * bout;

            // Tell orbis how many samples were consumed
            dspState.synthesis_read(bout);
        }

        return convOff;
    }

    /**
     * Decodes the next packet.
     *
     * @return bytes read into convbuffer of -1 if end of file
     */
    private int lazyDecodePacket() throws IOException {
        int result = getNextPacket();
        if (result == -1) {
            return -1;
        }

        // we have a packet.  Decode it
        return decodePacket();
    }

    private int getNextPacket() throws IOException {
        // get next packet.
        boolean fetchedPacket = false;
        while (!eos && !fetchedPacket) {
            int result1 = streamState.packetout(packet);
            if (result1 == 0) {
                // no more packets in page. Fetch new page.
                int result2 = 0;
                while (!eos && result2 == 0) {
                    result2 = syncState.pageout(page);
                    if (result2 == 0) {
                        fetchData();
                    }
                }

                // return if we have reaced end of file.
                if ((result2 == 0) && (page.eos() != 0)) {
                    return -1;
                }

                if (result2 == 0) {
                    // need more data fetching page..
                    fetchData();
                } else if (result2 == -1) {
                    logger.warn("syncState.pageout(page) result == -1");
                    return -1;
                } else {
                    streamState.pagein(page);
                }
            } else if (result1 == -1) {
                logger.warn("streamState.packetout(packet) result == -1");
                return -1;
            } else {
                fetchedPacket = true;
            }
        }

        return 0;
    }

    /**
     * Copys data from input stream to syncState.
     */
    private void fetchData() throws IOException {
        if (!eos) {
            // copy 4096 bytes from compressed stream to syncState.
            int bufferIndex = syncState.buffer(4096);
            if (bufferIndex < 0) {
                eos = true;
                return;
            }
            int bytes = in.read(syncState.data, bufferIndex, 4096);
            syncState.wrote(bytes);
            if (bytes == 0) {
                eos = true;
            }
        }
    }

    /**
     * Gets information on the ogg.
     */
    @Override
    public String toString() {
        String s = "";
        s = s + "version         " + info.version + "\n";
        s = s + "channels        " + info.channels + "\n";
        s = s + "rate (hz)       " + info.rate;
        return s;
    }
}
