The `Terasology` command has some options to control its initial configuration. Run `Terasology -h` for a list. Some of the options are documented in more detail below.


## Memory Usage

<dl>
<dt id="max-data-size">--max-data-size=N</dt>
<dd><p>Enforced by the operating system instead of the Java Virtual Machine, this limits memory usage in a different way than setting Java's maximum heap size (the <code>-Xmx</code> <a href="https://docs.oracle.com/en/java/javase/11/tools/java.html#GUID-3B1CE181-CD30-4178-9602-230B800D4FAE">java option</a>).
Use this to prevent Terasology from gobbling all your system memory if it has a memory leak.

Set this limit to a number larger than the maximum java heap size.
It is normal for a process to need <em>some</em> additional memory outside the java heap.

This value is in bytes, such as `2048M` or `4.7GB`.

This is currently only implemented on Linux.

On Windows, you may be able to set a limit using one of these external tools:
 - <a href="https://docs.microsoft.com/en-us/windows-hardware/drivers/devtest/application-verifier">Application Verifier (<code>AppVerif.exe</code>)</a>, available from the Windows SDK
 - <a href="https://github.com/lowleveldesign/process-governor">Process Governor (<code>procgov</code>)</a>, an open source third-party tool

</dd>
<dt id="oom-score">--oom-score=N</dt>
<dd><p>Make the Linux Out-of-Memory killer more likely to pick Terasology.

When a Linux system runs out of available memory, it invokes the Out of Memory killer (aka <i>OOM killer</i>) to choose a process to terminate to free up some memory.

Add **N** to this score if you want to make Terasology a bigger target.
Why? If you'd rather the game process be the thing that gets killed instead of some other memory-hungry program, like your browser or IDE.
A [score][proc5] of 1000 is equivalent to saying “this process is taking <em>all</em> the memory.”

This out-of-memory score is a Linux-specific mechanism.

[proc5]: https://man7.org/linux/man-pages/man5/proc.5.html#:~:text=/proc/%5Bpid%5D/-,oom_score_adj,-(since
</dd>
</dl>