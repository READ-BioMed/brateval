package au.com.nicta.csp.brateval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Document class, it includes implementations for entity and relation comparisons 
 * 
 * @author Antonio Jimeno Yepes (antonio.jimeno@gmail.com)
 *
 */
public class Document
{
  private Map <String, Entity> entities = new HashMap <String, Entity> ();
  private Map <String, Relation> relations = new HashMap <String, Relation> ();
  private Map <String, Event> events = new HashMap <String, Event> ();
  private List <Equivalent> equivalents = new LinkedList <Equivalent> ();
  private Map <String, Attribute> attributes = new HashMap <String, Attribute> ();
  private Map <String, Normalization> normalizations = new HashMap <String, Normalization> ();
  private Map <String, Note> notes = new HashMap <String, Note> ();

  public void addEntity(String id, Entity entity)
  { entities.put(id, entity); }
  
  public Entity getEntity(String id)
  { return entities.get(id); }

  public void removeEntity(String id)
  { entities.remove(id); }

  public Collection <Entity> getEntities()
  { return entities.values(); }
  
  public void addRelation(String id, Relation relation)
  { relations.put(id, relation); }
  
  public void removeRelation(String id)
  { relations.remove(id); }

  public Relation getRelation(String id)
  { return relations.get(id); }

  public Collection <Relation> getRelations()
  { return relations.values(); }
  
  public void addEvent(String id, Event event)
  { events.put(id, event); }
  
  public void removeEvent(String id)
  { events.remove(id); }
  
  public Event getEvent(String id)
  { return events.get(id); }

  public Collection <Event> getEvents()
  { return events.values(); }
  
  public void addEquivalent(Equivalent equivalent)
  { equivalents.add(equivalent); }

  public Collection <Equivalent> getEquivalents()
  { return equivalents; }
  
  public void addAttribute(String id, Attribute attribute)
  { attributes.put(id, attribute); }
  
  public void removeAttribute(String id)
  { attributes.remove(id); }
  
  public Attribute getAttribute(String id)
  { return attributes.get(id); }

  public Collection <Attribute> getAttributes()
  { return attributes.values(); }

  public void addNormalization(String id, Normalization normalization)
  { normalizations.put(id, normalization); }
  
  public void removeNormalization(String id)
  { normalizations.remove(id); }
  
  public Normalization getNormalization(String id)
  { return normalizations.get(id); }

  public Collection <Normalization> getNormalizations()
  { return normalizations.values(); }

  public void addNote(String id, Note note)
  { notes.put(id, note); }
  
  public void removeNote(String id)
  { notes.remove(id); }
  
  public Note getNote(String id)
  { return notes.get(id); }

  public Collection <Note> getNotes()
  { return notes.values(); }

  public Collection <Entity> getEntitiesByType(String type)
  {
	Collection <Entity> subentities = new ArrayList <Entity> ();

	for (Entity e : entities.values())
	{
	  if (e.getType().equals(type))
	  { subentities.add(e); }
	}

	return subentities;
  }
  
  public Entity findEntityOverlap(Entity e)
  {
    for (Entity e1 : entities.values())
    {
      if (Entity.entityComparisonOverlap(e, e1))
      { return e1; }
    }

	return null;
  }

  public Entity findEntityOverlapNoType(Entity e)
  {
    for (Entity e1 : entities.values())
    {
      if (Entity.entityComparisonSpanOverlap(e, e1))
      { return e1; }
    }

	return null;
  }
  
  public Entity findEntitySpanOverlap(Entity e)
  {
    for (Entity e1 : entities.values())
    {
      if (Entity.entityComparisonSpanOverlap(e, e1) && !e.equals(e1))
      { return e1; }
    }

	return null;
  }

  public boolean hasEntitySpanOverlapNC(Entity e)
  {
    for (Entity e1 : entities.values())
    {
      if (Entity.entityComparisonSpanOverlap(e, e1) && !e.equals(e1))
      { return true; }
    }

	return false;
  }

  public List <Entity> findAllEntitiesSpanOverlap(Entity e)
  {
	List <Entity> entities_span = new ArrayList <Entity> ();

    for (Entity e1 : entities.values())
    {
      if (Entity.entityComparisonSpanOverlap(e, e1) || Entity.entityComparisonSpanOverlap(e1, e))
      {
        if (!e.equals(e1))
        { entities_span.add(e1); }
      }
    }

	return entities_span;
  }
  
  public Entity findEntity(Entity e)
  {
    for (Entity e1 : entities.values())
    {
      if (Entity.entityComparison(e, e1) && !e.equals(e1))
      { return e1; }
    }

	return null;
  }

  public Entity findEntityOverlapC(Entity e)
  {
    for (Entity e1 : entities.values())
    {
      if (e1.getType().equals(e.getType()) && Entity.entityComparisonSpanOverlap(e, e1) && !e.equals(e1))
      { return e1; }
    }

	return null;
  }
  
  public Entity findEntitySimilarString(Entity e, double min_similarity) {
    Entity rc = null;
    for (Entity e1 : entities.values())
    {
      if (e1.getType().equals(e.getType()) && !e.equals(e1))
      {
      	double sim = Entity.entityComparisonStringSimilarity(e, e1, min_similarity);
      	if (sim >= min_similarity)
      	{
	      	min_similarity = sim;
	      	rc = e1;
      	}
  	  }
    }

	return rc;
  }
  /**
   * Find relation in a given document
   * 
   * @param relation
   * @param d
   * @return Return the relation in the matching document or null if no relation can be found
   */
  public Relation findRelationOverlap(Relation relation)
  {
    for (Relation rd : getRelations())
    {
      // Compare relation type
      if (relation.getRelationType().equals(rd.getRelationType())
      && (    		 
          Entity.entityComparisonOverlap(relation.getEntity1(), rd.getEntity1())
      &&  Entity.entityComparisonOverlap(relation.getEntity2(), rd.getEntity2())
    	 )
      )
      {
        // Compare entities
        return rd;
      }
    }

	return null;
  }

  /**
   * Find relation in a given document
   * 
   * @param relation
   * @param d
   * @return Return the relation in the matching document or null if no relation can be found
   */
  public Relation findRelation(Relation relation)
  {
    for (Relation rd : getRelations())
    {
      // Compare relation type
      if (relation.getRelationType().equals(rd.getRelationType())
      && ((
          Entity.entityComparison(relation.getEntity1(), rd.getEntity1())
      &&  Entity.entityComparison(relation.getEntity2(), rd.getEntity2())
    	 )
      // Order does not matter
      || (
           Entity.entityComparison(relation.getEntity1(), rd.getEntity2())
       &&  Entity.entityComparison(relation.getEntity2(), rd.getEntity1())
       	 )
      )
      )
      {
        // Compare entities
        return rd;
      }
    }

	return null;  
  }

  // Look for entity relation
  public boolean hasEntityRelation(Entity e)
  {
    for (Relation r : getRelations())
    {
      if (Entity.entityComparison(e, r.getEntity1()) || Entity.entityComparison(e, r.getEntity2()))
      { return true; }
    }

    return false;
  }
}