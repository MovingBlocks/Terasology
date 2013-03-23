/*
 * Copyright 2013 Moving Blocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.utilities;

import java.util.List;

// TODO: This class is needs to be refactored, with non-single character variable names
// TODO: Also someone check that we can actually use this, license-wise
// TODO: Are there some metrics suggesting this is better than java's built in sorting algorithms?
public final class Sorting {
    // Prevent instantiation
    private Sorting() {

    }

    // by keeping these constants, we can avoid the tiresome business
    // of keeping track of Dijkstra's b and c. Instead of keeping
    // b and c, I will keep an index into this array.

    static final int smoothSortLP[] = {1, 1, 3, 5, 9, 15, 25, 41, 67, 109,
            177, 287, 465, 753, 1219, 1973, 3193, 5167, 8361, 13529, 21891,
            35421, 57313, 92735, 150049, 242785, 392835, 635621, 1028457,
            1664079, 2692537, 4356617, 7049155, 11405773, 18454929, 29860703,
            48315633, 78176337, 126491971, 204668309, 331160281, 535828591,
            866988873 // the next number is > 31 bits.
    };

    public static <C extends Comparable<? super C>> void smoothSort(List<C> m) {
        if (m.size() > 1) {
            smoothSort(m, 0, m.size() - 1);
        }
    }

    // based on http://en.wikipedia.org/wiki/Smoothsort
    // The advantage of smoothsort is that it comes closer to O(n) time if the input is already sorted to some degree
    public static <C extends Comparable<? super C>> void smoothSort(List<C> m,
                                                                    int lo, int hi) {
        int head = lo; // the offset of the first element of the prefix into m

        // These variables need a little explaining. If our string of heaps
        // is of length 38, then the heaps will be of size 25+9+3+1, which are
        // Leonardo numbers 6, 4, 2, 1.
        // Turning this into a binary number, we get b01010110 = 0x56. We represent
        // this number as a pair of numbers by right-shifting all the zeros and
        // storing the mantissa and exponent as "p" and "pshift".
        // This is handy, because the exponent is the index into L[] giving the
        // size of the rightmost heap, and because we can instantly find out if
        // the rightmost two heaps are consecutive Leonardo numbers by checking
        // (p&3)==3

        int p = 1; // the bitmap of the current standard concatenation >> pshift
        int pshift = 1;

        while (head < hi) {
            if ((p & 3) == 3) {
                // Add 1 by merging the first two blocks into a larger one.
                // The next Leonardo number is one bigger.
                smoothSortSift(m, pshift, head);
                p >>>= 2;
                pshift += 2;
            } else {
                // adding a new block of length 1
                if (smoothSortLP[pshift - 1] >= hi - head) {
                    // this block is its final size.
                    smoothSortTrinkle(m, p, pshift, head, false);
                } else {
                    // this block will get merged. Just make it trusty.
                    smoothSortSift(m, pshift, head);
                }

                if (pshift == 1) {
                    // smoothSortLP[1] is being used, so we add use smoothSortLP[0]
                    p <<= 1;
                    pshift--;
                } else {
                    // shift out to position 1, add smoothSortLP[1]
                    p <<= (pshift - 1);
                    pshift = 1;
                }
            }
            p |= 1;
            head++;
        }

        smoothSortTrinkle(m, p, pshift, head, false);

        while (pshift != 1 || p != 1) {
            if (pshift <= 1) {
                // block of length 1. No fiddling needed
                int trail = Integer.numberOfTrailingZeros(p & ~1);
                p >>>= trail;
                pshift += trail;
            } else {
                p <<= 2;
                p ^= 7;
                pshift -= 2;

                // This block gets broken into three bits. The rightmost
                // bit is a block of length 1. The left hand part is split into
                // two, a block of length smoothSortLP[pshift+1] and one of smoothSortLP[pshift].
                // Both these two are appropriately heapified, but the root
                // nodes are not necessarily in order. We therefore semitrinkle
                // both of them

                smoothSortTrinkle(m, p >>> 1, pshift + 1, head - smoothSortLP[pshift] - 1, true);
                smoothSortTrinkle(m, p, pshift, head - 1, true);
            }

            head--;
        }
    }

    private static <C extends Comparable<? super C>> void smoothSortSift(List<C> m, int pshift,
                                                                         int head) {
        // we do not use Floyd's improvements to the heapsort smoothSortSift, because we
        // are not doing what heapsort does - always moving nodes from near
        // the bottom of the tree to the root.

        C val = m.get(head);

        while (pshift > 1) {
            int rt = head - 1;
            int lf = head - 1 - smoothSortLP[pshift - 2];

            if (val.compareTo(m.get(lf)) >= 0 && val.compareTo(m.get(rt)) >= 0)
                break;
            if (m.get(lf).compareTo(m.get(rt)) >= 0) {
                m.set(head, m.get(lf));
                head = lf;
                pshift -= 1;
            } else {
                m.set(head, m.get(rt));
                head = rt;
                pshift -= 2;
            }
        }

        m.set(head, val);
    }

    private static <C extends Comparable<? super C>> void smoothSortTrinkle(List<C> m, int p,
                                                                            int pshift, int head, boolean isTrusty) {

        C val = m.get(head);

        while (p != 1) {
            int stepson = head - smoothSortLP[pshift];

            if (m.get(stepson).compareTo(val) <= 0)
                break; // current node is greater than head. Sift.

            // no need to check this if we know the current node is trusty,
            // because we just checked the head (which is val, in the first
            // iteration)
            if (!isTrusty && pshift > 1) {
                int rt = head - 1;
                int lf = head - 1 - smoothSortLP[pshift - 2];
                if (m.get(rt).compareTo(m.get(stepson)) >= 0
                        || m.get(lf).compareTo(m.get(stepson)) >= 0)
                    break;
            }

            m.set(head, m.get(stepson));

            head = stepson;
            int trail = Integer.numberOfTrailingZeros(p & ~1);
            p >>>= trail;
            pshift += trail;
            isTrusty = false;
        }

        if (!isTrusty) {
            m.set(head, val);
            smoothSortSift(m, pshift, head);
        }
    }
}
