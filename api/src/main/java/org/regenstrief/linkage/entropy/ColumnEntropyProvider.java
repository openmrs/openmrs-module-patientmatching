package org.regenstrief.linkage.entropy;

/*
 * Class created to abstract the requirements of the
 * entropy calculating class.  This class needs to provide
 * a series of Strings, returning a null when all of the Strings
 * have been returned.
 */

public abstract class ColumnEntropyProvider {
	
	/*
	 * The method that returns the elemnts needed for the 
	 * entropy calculations
	 */
	public abstract String nextElement() throws EntropyProviderException;
	
	/*
	 * Returns whether a provider still has more elements to return
	 */
	public abstract boolean hasNext();
	
	/*
	 * Returns a boolean based on whether the provider can support
	 * the requirements of estimating the entropy instead of
	 * exact comprehension.  The sub class provider will need to know
	 * the number of elements in the whole set and the ability to randomly
	 * sample the set.
	 */
	public abstract boolean supportsEstimate();
}
