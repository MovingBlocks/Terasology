package org.terasology.maliciousUsageTests;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.paths.PathManager;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;

/**
 * @author Immortius
 */
@RegisterSystem
public class MaliciousTestSystem implements ComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(MaliciousTestSystem.class);

    private int numTests;
    private int passedTests;

    @Override
    public void initialise() {
        numTests = 0;
        passedTests = 0;
        testFileWrite();
        testReflectionConstruction();
        logger.info("Malicious Usage Test Result: {}/{} malicious usage tests passed", passedTests, numTests);
    }

    @Override
    public void shutdown() {
    }

    public void testFileWrite() {
        numTests++;
        try {
            Files.write(FileSystems.getDefault().getPath("garbage.out"), Lists.newArrayList("Blah", "Balh"), Charset.defaultCharset());
        } catch (IOException e) {
            logger.info("IO exception in test file write", e);
        } catch (NoClassDefFoundError | SecurityException e) {
            passedTests++;
        }
    }

    public void testReflectionConstruction() {
        numTests++;
        try {
            PathManager.getInstance();
        } catch (NoClassDefFoundError e) {
            passedTests++;
        }
    }

}
