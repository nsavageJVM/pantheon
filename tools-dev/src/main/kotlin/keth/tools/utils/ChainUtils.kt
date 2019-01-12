package keth.tools.utils

import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import tech.pegasys.pantheon.crypto.SECP256K1
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


abstract class  ProjectConnector {

    val panBaseProject = Paths.get(System.getProperty("user.home"),"pantheon" );
    val distTarPath =  Paths.get(System.getProperty("user.home"),"chains/private/pantheon-0.8.4-SNAPSHOT" );
    val baseProject = Paths.get(panBaseProject.toString(), "tools-dev")
    lateinit var connection : ProjectConnection
    lateinit var build: BuildLauncher


    fun getConnector(path: Path) {

        connection =  GradleConnector.newConnector().forProjectDirectory(path.toFile()).connect()
        build =  connection.newBuild();

    }
}


object InitChain : ProjectConnector() {


    fun initDirs() {

        super.getConnector(baseProject);
        try {
            build.forTasks("initDirs" );
            print("initDirs with project path "+Paths.get(panBaseProject.toString(), "tools-dev"))
            build.run();
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
        finally {
            connection.close();
        }

    }


    fun buildDistribution() {
        super.getConnector(panBaseProject);
        try {
            build.forTasks("clean", "build" );
            build.addArguments("-x", "test")
            build.run();
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
        finally {
            connection.close();
        }

    }

    fun copyDistribution() {

        super.getConnector(panBaseProject);
        try {
            build.forTasks("copyDistribution", "unpackDistribution");
            build.run();
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
        finally {
            connection.close();
        }

    }

    fun initBinDir( ){

        val dir = distTarPath.toFile()
        val newDir = File(dir.getParent() + File.separator + "peg")
        dir.renameTo(newDir)
    }
}


object KeyUtils {
    val bootNodeKeyPrivatePath = Paths.get(System.getProperty("user.home"),"chains/private/node1/cdata/key" );
    val bootNodeKeyPubPath = Paths.get(System.getProperty("user.home"),"chains/private/node1/cdata/publicKey" );

    fun generateKeysForBootNode() {
        val key  = SECP256K1.KeyPair.generate();
        var f = bootNodeKeyPrivatePath.toFile()

        if(!f.exists()){
            f.createNewFile();
        }
        key.privateKey.store(f)

        var g = bootNodeKeyPubPath.toFile()

        if(!g.exists()){
            g.createNewFile();
        }

        try {
            Files.newBufferedWriter(g.toPath(), UTF_8)
                    .use({ fileWriter -> fileWriter.write(key.getPublicKey().toString()) })
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}



fun main(args : Array<String>) {
    InitChain.initDirs()
    InitChain.buildDistribution()
    InitChain.copyDistribution()
    InitChain.initBinDir()
    InitChain.initBinDir()
    KeyUtils.generateKeysForBootNode()

}
