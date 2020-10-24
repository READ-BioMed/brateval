package au.com.nicta.csp.brateval;

public class MatchType {

    public enum SpanMatch {
		EXACT, // exact boundary match
		OVERLAP, // relaxed boundary match
		APPROXIMATE // relaxed boundary match, thresholded by edit-distance similarity
	}

 	public enum TypeMatch {
		EXACT, // exact type match
		INEXACT, // types differ
		HIERARCHICAL // types are in a hierarchical relationship
    }
    
    private SpanMatch spanMatchType;
    private TypeMatch typeMatchType;
	private double simThreshold = 1.0; // similarity threshold for approximate matching

	/**
	 * Default MatchType is EXACT match for both Span and Type.
	 */
	public MatchType() {
		spanMatchType = SpanMatch.EXACT;
		typeMatchType = TypeMatch.EXACT;
	}

	public MatchType(SpanMatch spanMatchType, TypeMatch typeMatchType) {
		this.spanMatchType = spanMatchType;
		this.typeMatchType = typeMatchType;
	}

	public MatchType(SpanMatch spanMatchType, TypeMatch typeMatchType, double simThreshold) {
		this.spanMatchType = spanMatchType;
		this.typeMatchType = typeMatchType;
		this.simThreshold = simThreshold;
	}


	public SpanMatch getSpanMatchType() {
		return this.spanMatchType;
	}

	public void setSpanMatchType(SpanMatch spanMatchType) {
		this.spanMatchType = spanMatchType;
	}

	public TypeMatch getTypeMatchType() {
		return this.typeMatchType;
	}

	public void setTypeMatchType(TypeMatch typeMatchType) {
		this.typeMatchType = typeMatchType;
	}

	public double getSimThreshold() {
		return this.simThreshold;
	}

	public void setSimThreshold(double simThreshold) {
		this.simThreshold = simThreshold;
	}

	public MatchType spanMatchType(SpanMatch spanMatchType) {
		this.spanMatchType = spanMatchType;
		return this;
	}

	public MatchType typeMatchType(TypeMatch typeMatchType) {
		this.typeMatchType = typeMatchType;
		return this;
	}

	public MatchType simThreshold(double simThreshold) {
		this.simThreshold = simThreshold;
		return this;
	}

	public String toString() {
		String str =  "Type match=" + this.typeMatchType + "; Span match=" + this.spanMatchType;
		if (this.spanMatchType.equals(SpanMatch.APPROXIMATE))
			str = str.concat("; Similarity=" + this.simThreshold);

		return str;
	}

}