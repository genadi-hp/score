package io.cloudslang.pypi;

import io.cloudslang.utils.UnzipUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.python.google.common.collect.Sets;
import org.python.google.common.io.Files;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 17/07/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = PipImplTest.TestConfig.class)
public class PipImplTest {
    static {
        ClassLoader classLoader = PipImplTest.class.getClassLoader();
        String pipPackagePath = classLoader.getResource("pip-8.1.2-py2.py3-none-any.whl").getPath();
        File rootHome = new File(pipPackagePath).getParentFile();
        File pythonLibHome = new File(rootHome, "python-lib");
        UnzipUtil.unzipToFolder(pythonLibHome.getAbsolutePath(), classLoader.getResourceAsStream("pip-8.1.2-py2.py3-none-any.whl"));

        System.setProperty(Pip.PYPI_REPOSITORY_PROPERTY, Pip.DEFAULT_PYPI_REPOSITORY);
        System.setProperty(Pip.PYTHON_LIB, pythonLibHome.getAbsolutePath());
    }

    @Autowired
    private Pip pip;

    @Test
    public void testJenkinsapi_0_2_30_wheels() {
        Set<String> expectedDependencies = Sets.newHashSet("jenkinsapi-0.2.30-py2-none-any.zip",
                "pytz-2016.6.1-py2.py3-none-any.zip",
                "requests-2.10.0-py2.py3-none-any.zip");
        checkDependencies("jenkinsapi", "0.2.30", expectedDependencies);
    }

    @Test
    public void testJenkinsapi_0_2_20_wheels() {
        Set<String> expectedDependencies = Sets.newHashSet("jenkinsapi-0.2.20-py2-none-any.zip",
                "pytz-2016.6.1-py2.py3-none-any.zip", "requests-2.10.0-py2.py3-none-any.zip",
                "pyasn1-0.1.9-py2.py3-none-any.zip", "pyopenssl-16.0.0-py2.py3-none-any.zip",
                "six-1.10.0-py2.py3-none-any.zip");
        checkDependencies("jenkinsapi", "0.2.20", expectedDependencies);
    }

    private void checkDependencies(String library, String version, Set<String> expectedDependencies) {
        File downloadFolder = Files.createTempDir();
        pip.download(library, version, downloadFolder.getAbsolutePath());

        File [] downloadedDependencies = downloadFolder.listFiles();

        for (File downloadedDependency : downloadedDependencies) {
            assertTrue("File [" + downloadedDependency.getName() + "] is not among expected [" + expectedDependencies + "]", expectedDependencies.contains(downloadedDependency.getName().toLowerCase()));
        }

        downloadFolder.deleteOnExit();
    }

    @Configuration
    @Import(PyPyConfiguraton.class)
    static class TestConfig {
    }
}
