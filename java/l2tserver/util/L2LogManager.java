package l2tserver.util;

import java.util.logging.LogManager;

/**
 * Dummy class to enable logs while shutting down
 *
 */
public class L2LogManager extends LogManager {
	
	public L2LogManager() {
		super();
	}
	
	@Override
	public void reset() {
		// do nothing
	}
	
	public void doReset() {
		super.reset();
	}
}