package org.rakam.aws.dynamodb;

import org.rakam.analysis.TestQueryMetastore;
import org.rakam.analysis.metadata.QueryMetadataStore;
import org.rakam.aws.AWSConfig;
import org.rakam.aws.dynamodb.apikey.DynamodbApiKeyConfig;
import org.rakam.aws.dynamodb.apikey.DynamodbApiKeyService;
import org.rakam.aws.dynamodb.metastore.DynamodbQueryMetastore;
import org.rakam.aws.dynamodb.metastore.DynamodbQueryMetastoreConfig;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Path;

import static com.google.common.collect.ImmutableList.of;
import static java.lang.String.format;
import static java.lang.System.getProperty;

public class TestDynamodbQueryMetastore extends TestQueryMetastore
{
    private final DynamodbQueryMetastore service;
    private Process dynamodbServer;

    public TestDynamodbQueryMetastore()
            throws Exception
    {
        int dynamodb = createDynamodb();
//        int dynamodb = 8000;
        service = new DynamodbQueryMetastore(new AWSConfig()
                .setAccessKey("AKIAIOSFODNN7EXAMPLE")
                .setSecretAccessKey("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY")
                .setDynamodbEndpoint("http://127.0.0.1:" + dynamodb),
                new DynamodbQueryMetastoreConfig().setTableName("query-metastore"));
    }

    @BeforeSuite
    public void setUp()
            throws Exception
    {
        service.setup();
    }

    @AfterSuite
    public void tearDown()
            throws Exception
    {
        dynamodbServer.destroy();
    }

    @Override
    public QueryMetadataStore getQuerymetastore()
    {
        return service;
    }

    public int createDynamodb()
            throws Exception
    {
        int randomPort = randomPort();
        Path mainDir = new File(getProperty("user.dir"), ".test/dynamodb").toPath();

        dynamodbServer = new ProcessBuilder(of("java", format("-Djava.library.path=%s",
                mainDir.resolve("DynamoDBLocal_lib").toFile().getAbsolutePath()),
                "-jar", mainDir.resolve("DynamoDBLocal.jar").toFile().getAbsolutePath(),
                "-inMemory", "--port", Integer.toString(randomPort)))
                .start();

        return randomPort;
    }

    private static int randomPort()
            throws IOException
    {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
