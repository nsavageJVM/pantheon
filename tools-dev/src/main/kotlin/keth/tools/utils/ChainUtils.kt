package keth.tools.utils

import keth.tools.Constants
import keth.tools.KeyUtils
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


    val distTarPath =  Paths.get(System.getProperty("user.home"),"chains/private/pantheon-0.8.4-SNAPSHOT" );
    val baseProject = Constants.panBaseProject.resolve("tools-dev")

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
            print("initDirs with project path "+Paths.get(Constants.panBaseProject.toString(), "tools-dev"))
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
        super.getConnector(Constants.panBaseProject);
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

        super.getConnector(Constants.panBaseProject);
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


fun main(args : Array<String>) {
    InitChain.initDirs()
    InitChain.buildDistribution()
    InitChain.copyDistribution()
    InitChain.initBinDir()
    KeyUtils.generateKeysForBootNode()

    var scriptPath =  Constants.bootNodePath.resolve("run_boot_node.sh")

    BashProvider.writeScript(scriptPath, TYPE.BOOT)

    scriptPath =  Constants.bootNode2.resolve("run_node_2.sh")

    BashProvider.writeScript(scriptPath, TYPE.NODE2)

    scriptPath =  Constants.bootNode3.resolve("run_node_3.sh")

    BashProvider.writeScript(scriptPath, TYPE.NODE3)

}
