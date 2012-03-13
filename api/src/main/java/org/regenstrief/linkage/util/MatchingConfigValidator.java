package org.regenstrief.linkage.util;

/**
 * Class implements a method to check a given blocking run is valid.  It checks for:
 * at least one blocking column
 * at least one included column
 * blocking columns must be in order, not skipping rank
 * @author jegg
 *
 */

public class MatchingConfigValidator {

	public static boolean validMatchingConfig(MatchingConfig mc){
		String[] cols = mc.getBlockingColumns();
		if(cols == null){
			return false;
		}
		
		boolean has_blocking = cols.length > 0;
		
		boolean valid_blocking = false;
		boolean[] order_present = new boolean[cols.length];
		try{
			for(int i = 0; i < cols.length; i++){
				String col = cols[i];
				MatchingConfigRow mcr = mc.getMatchingConfigRowByName(col);
				order_present[mcr.getBlockOrder() - 1] = true;
			}
			boolean all_true = true;
			for(int i = 0; i < order_present.length; i++){
				all_true = all_true && order_present[i];
			}
			valid_blocking = all_true;
		}
		catch(ArrayIndexOutOfBoundsException aioobe){
			valid_blocking = false;
		}
		
		boolean valid_include = mc.getIncludedColumnsNames() != null;
		
		return has_blocking && valid_blocking && valid_include;
	}
}
