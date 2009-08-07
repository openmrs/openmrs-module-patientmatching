# Summarize a pipe-delimited patient database with pairwise metrics (e.g.
# mutual information)

# Author: Shaun Grannis
# (originally h_tab.R)

require('entropy')

pairmet <- function(df,rofs,cofs) {	
    cat("row|")

    for (col in cofs:length(df)) {
        cat(names(df[col]), "|", sep="")
    }
    
    cat("\n")

    # compute the mutual information of each pair of fields
    for ( row in (rofs+1):(length(df)) ) {
      	cat(names(df[row]), "|" , sep="")

      	for ( col in cofs:(row-1) ) {
	    cat ( entropy(table(factor(do.call(paste, c(df[c(row,col)], sep = "|")))),method="ML",unit="log2"), "|" , sep="" )
	}

        cat("\n")
    }
}
