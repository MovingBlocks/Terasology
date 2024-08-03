I/O API for Modules
=================

To protect the user's system, you cannot use `java.io.File` directly, however a bunch of modules may need access to files. Finally, this feature allow modules to have **limited** access to files. Basically, there are two file operations allowed, `readFile` and `writeFile`, both of them works under the `Terasology/sandbox` directory. Take a look on the instructions bellow to learn how to use them.

1. The first step is to import and initialize the sandbox file manager where you need it, for this you can do the following:
```java
SandboxFileManager sandboxFileManager = new SandboxFileManager();
```
2. Second, create a consumer. We have two different types of consumers.
* Read file consumer:
```java
// put whatever you need here
byte[] someBytes = new byte[2];

Consumer<OutputStream> consumer = outputStream -> {
    try {
        outputStream.write(someBytes);
    } catch (IOException e) {
        // error
    }
};
```
* Write file consumer:
```java
Consumer<InputStream> consumer = inputStream -> {
    try {
       int value = inputStream.read();
           
       while (value != -1) {
           doSomething(value); // call your method here
           value = inputStream.read();
        }
    } catch (IOException e) {
        // error
    }
};
```
3. The third and final step is to call any of the `SandboxFileManager` methods you need. For both methods, it is mandatory to pass in their respective consumer and the file name.
* Read file method:
```java
sandboxFileManager.readFile("file.txt", consumer);
```
* Write file method:
```java
sandboxFileManager.writeFile("file.txt", consumer);
```

Finally, if you wrote any file, you can see them in `Terasology/sandbox` directory. :smiley: 