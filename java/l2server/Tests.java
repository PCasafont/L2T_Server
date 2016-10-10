package l2server;

import l2server.gameserver.datatables.ItemTable;
import l2server.gameserver.datatables.SkillTable;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.logging.LogManager;

/**
 * @author Pere
 */
public class Tests
{
    private void initializeServer()
    {
        ServerMode.serverMode = ServerMode.MODE_GAMESERVER;
        // Local Constants
        final String LOG_FOLDER = "log"; // Name of folder for log file
        final String LOG_NAME = "./log.cfg"; // Name of log file

        /*** Main ***/
        // Create log folder
        File logFolder = new File(Config.DATAPACK_ROOT, LOG_FOLDER);
        logFolder.mkdir();

        try (InputStream is = new FileInputStream(new File(LOG_NAME)))
        {
            // Create input stream for log file -- or store file data into memory
            LogManager.getLogManager().readConfiguration(is);
        }
        catch (Exception e)
        {
        }

        // Initialize config
        Config.load();
    }

    @Test
    public void testSkillLoading()
    {
        initializeServer();
        SkillTable.getInstance();
    }

    @Test
    public void testItemLoading()
    {
        initializeServer();
        ItemTable.getInstance();
    }
}
