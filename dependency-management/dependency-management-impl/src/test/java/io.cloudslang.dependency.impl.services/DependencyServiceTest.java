package io.cloudslang.dependency.impl.services;

import io.cloudslang.dependency.api.services.DependencyService;
import io.cloudslang.dependency.api.services.MavenConfig;
import io.cloudslang.dependency.impl.services.utils.UnzipUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * @author A. Eskin
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DependencyServiceTest.TestConfig.class)
public class DependencyServiceTest {
    private static boolean shouldRunMaven;
    static  {
        ClassLoader classLoader = DependencyServiceTest.class.getClassLoader();
        @SuppressWarnings("ConstantConditions") String settingsXmlPath = classLoader.getResource("settings.xml").getPath();
        File rootHome = new File(settingsXmlPath).getParentFile();
        File mavenHome = new File(rootHome, "maven");
        UnzipUtil.unzipToFolder(mavenHome.getAbsolutePath(), classLoader.getResourceAsStream("maven.zip"));

        System.setProperty(MavenConfigImpl.MAVEN_HOME,  mavenHome.getAbsolutePath());

        shouldRunMaven = System.getProperties().containsKey(MavenConfigImpl.MAVEN_REMOTE_URL) &&
                System.getProperties().containsKey(MavenConfigImpl.MAVEN_PLUGINS_URL);
        System.setProperty(MavenConfigImpl.MAVEN_REPO_LOCAL, new TestConfig().mavenConfig().getLocalMavenRepoPath());

        //noinspection ConstantConditions
        System.setProperty("maven.home", classLoader.getResource("maven").getPath());

        String localRepository = System.getProperty(MavenConfigImpl.MAVEN_REPO_LOCAL);
        if (localRepository != null && !localRepository.isEmpty()) {
            System.setProperty("maven.repo.local", localRepository);
        }

        System.setProperty(MavenConfigImpl.MAVEN_SETTINGS_PATH, settingsXmlPath);
        //noinspection ConstantConditions
        System.setProperty(MavenConfigImpl.MAVEN_M2_CONF_PATH, classLoader.getResource("m2.conf").getPath());
    }


    @Autowired
    private DependencyService dependencyService;

    @After
    public void cleanup() {
        String basePath = new TestConfig().mavenConfig().getLocalMavenRepoPath();
        new File(basePath + "/junit/junit/4.12/junit-4.12.path").delete();
        new File(basePath + "/groupId1/mvn_artifact1/1.0/mvn_artifact1-1.0.path").delete();
    }

    @Test
    public void testMultipleDependencyResolution() {
        Set<String> ret = dependencyService.getDependencies(new HashSet<>(Arrays.asList("groupId1:test-artifact:1.0",
                "groupId1:test-artifact1:1.1")));
        List<String> referenceList = Arrays.asList("C:/aaa/bbb/ccc.jar", "C:/bbb/ccc/ddd.zip", "C:/ccc/ddd/eee/fff.jar",
                "C:/aaaa/bbbb/cccc.jar", "C:/bbbb/cccc/dddd.zip");
        Assert.assertTrue("Unexpected returned set", ret.containsAll(referenceList) && ret.size() == referenceList.size());
    }

    @Test
    public void testSingleDependencyResolution() {
        Set<String> ret = dependencyService.getDependencies(new HashSet<>(Collections.singletonList("groupId1:test-artifact1:1.1")));
        List<String> referenceList = Arrays.asList("C:/aaaa/bbbb/cccc.jar", "C:/bbbb/cccc/dddd.zip");
        Assert.assertTrue("Unexpected returned set", ret.containsAll(referenceList) && ret.size() == referenceList.size());
    }

    @Test
    public void testEmptyResourceSet() {
        Set<String> ret1 = dependencyService.getDependencies(new HashSet<String>());
        Assert.assertTrue("Unexpected returned set", ret1.isEmpty());
    }

    @Test
    public void testMalformedGav() {
        try {
            dependencyService.getDependencies(new HashSet<>(Collections.singletonList("groupId1:test-artifact1")));
            Assert.fail("Expected IllegalArgumentException, but succeeded");
        } catch (IllegalArgumentException ignore) {

        }
    }

    @Test
    public void testBuildClassPath1() {
        Assume.assumeTrue(shouldRunMaven);
        Set <String> ret = dependencyService.getDependencies(new HashSet<>(Collections.singletonList("groupId1:mvn_artifact1:1.0")));
        final List<File> retFiles = new ArrayList<>();
        for (String s : ret) {
            retFiles.add(new File(s));
        }
        String basePath = new TestConfig().mavenConfig().getLocalMavenRepoPath();
        List<File> referenceList = Arrays.asList(
                new File(basePath + "/junit/junit/4.12/junit-4.12.jar"),
                new File(basePath + "/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar"),
                new File(basePath + "/org/springframework/spring-core/4.2.5.RELEASE/spring-core-4.2.5.RELEASE.jar"),
                new File(basePath + "/commons-logging/commons-logging/1.2/commons-logging-1.2.jar"),
                new File(basePath + "/groupId1/mvn_artifact1/1.0/mvn_artifact1-1.0.jar"));
        Assert.assertTrue("Unexpected returned set", retFiles.containsAll(referenceList) && ret.size() == referenceList.size());
    }

    @Test
    public void testBuildClassPath1_1() {
        Assume.assumeTrue(shouldRunMaven);
        Set <String> ret = dependencyService.getDependencies(new HashSet<>(Collections.singletonList("groupId1:mvn_artifact1:1.1")));
        final List<File> retFiles = new ArrayList<>();
        for (String s : ret) {
            retFiles.add(new File(s));
        }
        String basePath = new TestConfig().mavenConfig().getLocalMavenRepoPath();
        List<File> referenceList = Collections.singletonList(
                new File(basePath + "/groupId1/mvn_artifact1/1.1/mvn_artifact1-1.1.jar"));
        Assert.assertTrue("Unexpected returned set", retFiles.containsAll(referenceList) && ret.size() == referenceList.size());
    }

    @Test
    public void testBuildClassPath1_2() {
        Assume.assumeTrue(shouldRunMaven);
        Set <String> ret = dependencyService.getDependencies(new HashSet<>(Collections.singletonList("groupId1:mvn_artifact1:1.2")));
        final List<File> retFiles = new ArrayList<>();
        for (String s : ret) {
            retFiles.add(new File(s));
        }
        String basePath = new TestConfig().mavenConfig().getLocalMavenRepoPath();
        List<File> referenceList = Collections.singletonList(
                new File(basePath + "/groupId1/mvn_artifact1/1.2/mvn_artifact1-1.2.jar"));
        Assert.assertTrue("Unexpected returned set", retFiles.containsAll(referenceList) && ret.size() == referenceList.size());
    }

    @Test
    public void testBuildClassPath2() {
        Assume.assumeTrue(shouldRunMaven);
        String basePath = new TestConfig().mavenConfig().getLocalMavenRepoPath();
        File junitArtifactDir = new File(basePath + "/junit");
        if(junitArtifactDir.exists()) {
            boolean isDeleted1 = new File(basePath + "/junit/junit/4.12/junit-4.12.jar").delete();
            boolean isDeleted2 = new File(basePath + "/junit/junit/4.12/junit-4.12.pom").delete();
            Assert.assertTrue(isDeleted1 && isDeleted2);
        }
        Set <String> ret = dependencyService.getDependencies(new HashSet<>(Collections.singletonList("junit:junit:4.12")));
        final List<File> retFiles = new ArrayList<>();
        for (String s : ret) {
            retFiles.add(new File(s));
        }
        List<File> referenceList = Arrays.asList(
                new File(basePath + "/junit/junit/4.12/junit-4.12.jar"),
                new File(basePath + "/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar"));
        Assert.assertTrue("Unexpected returned set", retFiles.containsAll(referenceList) && ret.size() == referenceList.size());
    }


    @Configuration
    static class TestConfig {
        @Bean
        public DependencyService dependencyService() {
            return new DependencyServiceImpl();
        }

        @Bean
        public MavenConfig mavenConfig() {
            return new MavenConfig() {
                @Override
                public String getLocalMavenRepoPath() {
                    String testMvnRepo = "test-mvn-repo";
                    URL url = getClass().getClassLoader().getResource(testMvnRepo);
                    if(url != null) {
                        return url.getPath();
                    }
                    return null;
                }

                @Override
                public String getRemoteMavenRepoUrl() {
                    return null;
                }
            };
        }

    }

}
