# Summarize a pipe-delimited patient database dump with metrics specific to
# individual, single fields

# Author: Shaun Grannis
# (originally h_list.R)

require('entropy')

singmet <- function(df) {	
    # Total Records
    tr01 <- length(df[[1]]);
    cat("Total Recs|", tr01[1], "\n", sep="")

    # Header row
    cat("Col|H|Hmax|Hmax%|UqVal|Favg|N|N%|Uval|pairs|log(pairs)\n",sep="")

    for (col in 1:length(df)) {
        #### Calculate metrics ####

	# Calculate Shannon's entropy (H)
	ent01 <- entropy(table(factor(do.call(paste, c(df[c(col)], sep = "|")))),method="ML",unit="log2")

	# Calculate Unique Values (UqVal)
	uqval <- length( unique(df[[col]]) )

	# Calculate NULLs (N)
	null01 <- length( df[[col]][ df[[col]] == '' ])

	# Calculate Favg
	favg01 <- tr01[1] / uqval[1]

	# Calculate Maximum Entropy
	mxent <- -( (favg01[1] / tr01[1]) * log( favg01[1] / tr01[1]) /log(2) ) * uqval 

	# Closed-form u-value
	uval01 <- sum((table(factor(df[[col]]))/tr01[1])^2)

	# Potential pairs formed if used as single blocking variable
	prs01 <- sum(  (  (table(factor(df[[col]])))^2 - (table(factor(df[[col]])))  ) / 2 )

	#### Output metrics ####

        # Output Column Name
	cat( names(df[col]), "|", sep="")

	# Output Shannon's entropy (H)
	cat( ent01[1], "|" , sep="") 

	# Output maximum entropy (Hmax)
	cat(mxent[1], "|", sep="")

	# Output maximum entropy percent (Hmax%)
	cat(ent01[1] / mxent[1], "|", sep="")

	# Output Unique Values (UqVal)
	cat( uqval[1], "|", sep="")

	# Output Average Frequency (Favg)
	cat( favg01[1], "|", sep="")

	# Output NULLs (N)
	cat( null01[1], "|", sep="" )

	# Output NULL percent (N%)
	cat( null01[1] / tr01[1], "|", sep="" )

	# Output closed-form U-value (Uval)
	cat( uval01[1], "|", sep="")

	# Output pairs formed if used as single blocking variable
	cat(prs01[1], "|", sep="")

	# Output LOG(pairs formed) if used as single blocking variable
	cat(log(prs01[1])/log(10), "\n", sep="")
  }
}
