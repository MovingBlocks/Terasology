* The most common cause for game crashes is older video cards, *especially* Intel HD Graphics on laptops. Make sure you have the very latest graphics drivers available.
* If you're using a laptop that has a discrete NVIDIA GPU and Intel HD Graphics, the game may be attempting to run on the latter, causing a crash. To make the game use the discrete GPU,
  * Right-click on your desktop, select NVIDIA Control Panel. If the option doesn't show up, [install the latest NVIDIA drivers](http://www.geforce.com/drivers).
  * Select *Manage 3D settings*.
  * Select *Program Settings*, then navigate to wherever the Terasology executable is located and select it.
    * **Note:** This used to work when Terasology shipped with a `.exe` file for Windows - we have since switched to a `.bat` which does not seem eligible for this workaround. Additionally it won't work when running the game from source (possibly unless you apply it system-wide to any execution of `java.exe` which is likely not ideal ..) - this may take some Googling and updating of this page if anybody finds better options!
  * In the processor selection dropdown, choose *High-performance NVIDIA processor* and click Apply.
* You may run out of memory if your system doesn't have much available or you run the game with too little memory assigned and too high view distance. Especially with a 32-bit system or Java (has failed on even taking large screenshots in the past)
  * To run with increased memory you can launch via command prompt / terminal with something like: `java -Xms128m -Xmx2048m -jar libs/Terasology.jar` which will assign a max of 2 GB (but crash with a 32-bit Java! Can't assign more than around 1.5 GB)
* You may get odd OpenAL warnings/crashes if you have an unusual audio setup. One user reported a crash after having set up a virtual audio cable for streaming purposes. That error specifically was an `IllegalStateException` over there being no OpenAL context available.

## Proxy Settings

**Prerequisite:** You are behind a proxy.

**Symptoms:** You face connection issues in the Terasology Launcher or Terasology itself.

**Solution:** Properly configure your proxy settings in your local *Java Control Panel*. For more information, please refer to https://www.java.com/en/download/help/proxy_setup.html.