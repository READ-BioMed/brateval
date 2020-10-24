package au.com.nicta.csp.brateval;


import au.com.nicta.csp.brateval.MatchType.SpanMatch;
import au.com.nicta.csp.brateval.MatchType.TypeMatch;

public class EntityMatchResult {

    private boolean match;
    private MatchType matchType;
    private double matchSim;

    public MatchType getMatchType() {
        return this.matchType;
    }

    public void setMatchType(MatchType matchType) {
        this.matchType = matchType;
    }

    public double getMatchSim() {
        return this.matchSim;
    }

    public void setMatchSim(double matchSim) {
        this.matchSim = matchSim;
    }

    private Entity e1;
    private Entity e2;

    public EntityMatchResult() {
        matchType = new MatchType();
        matchSim = 1.0;
        this.match = true;
    }

    public EntityMatchResult(boolean match, SpanMatch spanMatchType, TypeMatch typeMatchType, Entity e1, Entity e2) {
        this.match = match;
        matchType = new MatchType(spanMatchType, typeMatchType);
        this.e1 = e1;
        this.e2 = e2;
        matchSim = 1.0;
    }

    public EntityMatchResult match(boolean match) {
        this.match = match;
        return this;
    }

    public EntityMatchResult e1(Entity e1) {
        this.e1 = e1;
        return this;
    }

    public EntityMatchResult e2(Entity e2) {
        this.e2 = e2;
        return this;
    }

    public SpanMatch getSpanMatchType() {
        return matchType.getSpanMatchType();
    }

    public void setSpanMatchType(SpanMatch spanMatchType) {
        matchType.setSpanMatchType(spanMatchType);
    }

    public TypeMatch getTypeMatchType() {
        return matchType.getTypeMatchType();
    }

    public void setTypeMatchType(TypeMatch typeMatchType) {
        matchType.setTypeMatchType(typeMatchType);
    }

    public boolean getMatch() {
        return this.match;
    }

    public boolean isMatch() {
        return this.match;
    }

    public void setMatch(boolean match) {
        this.match = match;
    }

    public Entity getE1() {
        return this.e1;
    }

    public void setE1(Entity e1) {
        this.e1 = e1;
    }

    public Entity getE2() {
        return this.e2;
    }

    public void setE2(Entity e2) {
        this.e2 = e2;
    }

    /**
     * Summarises EntityMatchResult in a string.
     * Only prints details for non-Exact matches, otherwise null is returned.
     */
    public String toString() {
        String str = null;

        if ( matchType.getSpanMatchType() != SpanMatch.EXACT ) { // Inexact Span
            String inexactPrefix = "Inexact-Span";
            if ( matchType.getSpanMatchType().equals(SpanMatch.APPROXIMATE) )
                inexactPrefix = "Approx-Span";
            if ( matchType.getTypeMatchType() != TypeMatch.EXACT ) { // Inexact Span + Inexact Type
                // TODO: Work out whether the types are related (i.e., one more specific than the other) or a 'clash'
                if ( e1.getType().compareTo(e2.getType()) > 0 ) { // order types consistently
                    str = "DOCUMENT:" + e1.getFile() + "|" + inexactPrefix + "and-TYPE|" + e1.getType() + "|" + e1.locationInfo() + "|" + e1.getString() + " |~| " + e2.getType() + "|" + e2.locationInfo() + "|" + e2.getString();
                } else {
                    str = "DOCUMENT:" + e1.getFile() + "|" + inexactPrefix + "-and-TYPE|" + e2.getType() + "|" + e2.locationInfo() + "|" + e2.getString() + " |~| " + e1.getType() + "|" + e1.locationInfo() + "|" + e1.getString();
                }
            } else { // inexact Span + exact Type
                str = "DOCUMENT:" + e1.getFile() + "|" + inexactPrefix + "|" + e1.getType() + "|" + e1.locationInfo() + "|" + e1.getString() + " |~| " + e2.getType() + "|" + e2.locationInfo() + "|" + e2.getString();
            }
        } else { // exact Span
            if ( matchType.getTypeMatchType() != TypeMatch.EXACT ) { // inexact Type
                if ( e1.getType().compareTo(e2.getType()) > 0 ) {
                    str = "DOCUMENT:" + e1.getFile() + "|" + "Inexact-TYPE|" + e1.getType() + "|" + e1.locationInfo() + "|" + e1.getString() + " |~| " +e2.getType() + "|" + e2.locationInfo() + "|" + e2.getString();
                } else {
                    str ="DOCUMENT:" + e2.getFile() + "|" + "Inexact-TYPE|" + e2.getType() + "|" + e2.locationInfo() + "|" + e2.getString() + " |~| " + e1.getType() + "|" + e1.locationInfo() + "|" + e1.getString();
                }
            }
        }	

        return str;						
    }

}
