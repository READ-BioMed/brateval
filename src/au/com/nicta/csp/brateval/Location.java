package au.com.nicta.csp.brateval;

public class Location
{
  private int start;
  private int end;

  public Location (int start, int end)
  {
    this.start = start;
    this.end = end;
  }

  public int getStart()
  { return start; }

  public int getEnd()
  { return end; }
}
