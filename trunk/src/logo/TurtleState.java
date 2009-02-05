
package logo;

import simplerjogl.*;

/**
 * The Turtle represents the cursor in the Logo environment. Logo drawings
 * are made by passing commands (move, pen up, pen down, turn, etc.) to the
 * turtle. The sequence of changes to the turtle's state are replicated to
 * present the drawing. The TurtleState is a record of the turtle's state
 * at a single instant in the sequence of instructions, storing its
 * location, heading, color, and pen state.
 * 
 * @author Seth Battis
 * @version 1.0
 */
public class TurtleState
{
	/**
	 * The current position of the turtle in 2-D cartesian coordinates,
	 * relative to an origin at the center of the screen.
	 */
	protected Vertex position;

	/**
	 * The "other" corner (if a shape such as a rectangle has been drawn).
	 * The position is the top-left corner o the bounding box, while the
	 * "other" corner is the bottom-right.
	 */
	protected Vertex otherCorner;
	/**
	 * The direction in which the turtle is currently headed, measured in
	 * degrees, with 0 degrees heading due East, 90 due North and so forth.
	 */
	protected int heading;

	/**
	 * Whether or not the turtle's pen is placed on the paper -- is the
	 * turtle leaving a line behind it as it moves?
	 */
	protected boolean penDown;

	/**
	 * The current color of the turtle's pen.
	 */
	protected Color penColor;

	/**
	 * Default constructor. The default turtle state is at the origin,
	 * headed 0 degrees (due East), pen down, with a white pen.
	 */
	public TurtleState ()
	{
		this (new Vertex (0, 0), 0, true, new Color (255, 255, 255));
	}

	/**
	 * Construct a turtle state with specific settings. (The "other" corner
	 * is null -- no shape with a bounding box is drawn.)
	 */
	public TurtleState (Vertex position, int heading, boolean penDown, Color penColor)
	{
		this (position, null, heading, penDown, penColor);
	}

	/**
	 * Construct a turtle state representing a shape with a bounding box.
	 * The position is the top-left corner of the shape's bounding box and
	 * the "other" corner is the bottom-right corner of the bounding box.
	 */
	public TurtleState (Vertex position, Vertex otherCorner, int heading, boolean penDown, Color penColor)
	{
		this.position = new Vertex (position);
		this.otherCorner = (otherCorner != null ? new Vertex (otherCorner) : null);
		this.heading = heading;
		this.penDown = penDown;
		this.penColor = new Color (penColor);
	}

	/**
	 * Does this turtle state represent an instruction to draw a shape with
	 * a bounding box?
	 */
	public boolean isRect ()
	{
		return otherCorner != null;
	}

	/**
	 * Are the settings of the turtle states identical?
	 */
	public boolean equals (TurtleState other)
	{
		if (other == null)
		{
			return false;
		}
		else
		{
			boolean equal = (position.equals (other.position) && heading == other.heading && penDown == other.penDown && penColor.equals (other.penColor));
			if ( (otherCorner == null) && (other.otherCorner == null))
			{
				return equal;
			}
			else if ( (otherCorner != null) && (other.otherCorner != null))
			{
				return equal && otherCorner.equals (other.otherCorner);
			}
			else
			{
				return false;
			}
		}
	}

	public Vertex getPosition ()
	{
		return new Vertex (position);
	}

	public int getX ()
	{
		return (int) position.getX ();
	}

	public int getY ()
	{
		return (int) position.getY ();
	}

	public Vertex getOtherCorner ()
	{
		return new Vertex (otherCorner);
	}

	public int getHeading ()
	{
		return heading;
	}

	public boolean getPenDown ()
	{
		return penDown;
	}

	public Color getPenColor ()
	{
		return new Color (penColor);
	}

	public String toString ()
	{
		return new String ("pos " + position.toString2D () + ", head " + heading + ", pen " + (penDown ? "down" : "up") + ", color " + penColor.toStringRGB ());
	}
}