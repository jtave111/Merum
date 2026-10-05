package com.manager.server.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.io.TempDir;

public class LocalNetworkFingerprintServiceTest {

    @TempDir
    Path tempDir;
/*    @Test
    void testExecBinaryWithFlag() throws Exception {

        LocalNetworkFingerprintService service = new LocalNetworkFingerprintService(){
            @Override
            public File getRootPath(){
                return tempDir.toFile();
            }
        };

        Path pathStructure = tempDir.resolve("modules/linux/c++/code/localFingerPrint/");
        Files.createDirectories(pathStructure);
    
        File fakeBinary = new File(pathStructure.toFile(), "BinaryTest");
        String scriptContent = "#!/bin/bash\necho \"[MOCK] Executed with: $@\"";

        Files.write(fakeBinary.toPath(), scriptContent.getBytes());
        fakeBinary.setExecutable(true);

      String result = service.excLocalNetFingerPrint("BinaryTest", "all");

        System.out.println("DEBUG - from Test:\n" + result);

        assertNotNull(result, "The result must not be null");

        assertTrue(result.contains("[MOCK] Executed with:"), "The mock binary did not return the expected string");
        assertTrue(result.contains("--create_session"), "Missing flag --create_session");
        assertTrue(result.contains("-all-ports"), "Missing flag -all-ports");
    }
         */

}
