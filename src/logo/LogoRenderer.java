
package logo;

import java.util.ArrayList;

import javax.media.opengl.GL2;

import simplerjogl.Color;
import simplerjogl.Material;
import simplerjogl.Vertex;
import simplerjogl.shell.ShellEvent;
import simplerjogl.simplerjogl2d.Renderer2D;

/**
 * The actual Logo-style drawing environment. The renderer draws the
 * sequence of turtle states on the screen (using SimplerJOGL for support)
 * and processes user input via the SimplerJOGL shell to construct
 * additional components of the drawing.
 * 
 * @author Seth Battis
 * @version 1.0
 */
public class LogoRenderer extends Renderer2D
{
	/* The vocabulary of the Logo instructions. */
	private static final int MOVE = 0, HEADING = 1, FORWARD = 2, BACK = 3, TURN_LEFT = 4, TURN_RIGHT = 5, PEN_UP = 6, PEN_DOWN = 7, PEN_COLOR = 8, BACKGROUND = 9, RESET = 10, UNDO = 11, RECTANGLE = 12;
	protected static final String[] commands = { "mv", "hd", "fw", "bk", "tl", "tr", "pu", "pd", "pc", "bg", "rs", "un", "re", };

	/**
	 * The current state of the turtle in the drawing environment
	 */
	private TurtleState turtle;

	/**
	 * The sequence of all previous turtle states that have constructed the
	 * drawing.
	 */
	private ArrayList<TurtleState> vertices;

	/**
	 * The current background color.
	 */
	private Color background;

	/**
	 * Default constructor. Open an empty drawing environment with all
	 * settings at their reset (default) configuration.
	 * 
	 * @see #reset()
	 */
	public LogoRenderer ()
	{
		super ();
		reset ();
	}

	/**
	 * Initialization instructions executed only once at the start of the
	 * application's run. The script() method (provided for the LogoScript
	 * extension of the renderer) is called, followed by starting the
	 * shell's text processor.
	 * 
	 * @see #script()
	 */
	public void init ()
	{
		script ();
		if (!shell.isMidRead ())
		{
			shell.readln ();
		}
	}

	/**
	 * An empty method that can be over-ridden in the LogoScript extension
	 * of this renderer to provide initial drawing instructions to the
	 * environment.
	 */
	public void script ()
	{}

	/**
	 * The OpenGL event handler -- this redraws the screen each time it
	 * refreshes, plotting the sequence of turtle states to create the
	 * drawing.
	 */
	public void display ()
	{
		/* set the background color */
		float rgba[] = background.getRGBAf ();
		gl.glClearColor (rgba[0], rgba[1], rgba[2], rgba[3]);

		/* set a visible line width to follow the turtle's pen movements */
		gl.glLineWidth (2);

		/* plot a line following the turtle's movements */
		gl.glBegin (GL2.GL_LINE_STRIP);
		{
			for (TurtleState v : vertices)
			{
				/* is there a visual record of this state? */
				if (v.getPenDown ())
				{
					/* use this state's pen color */
					new Material (gl, v.getPenColor ()).use ();

					/* rectangles are a special case turtle state */
					if (v.isRect ())
					{
						double left = v.getPosition ().getX ();
						double right = v.getOtherCorner ().getX ();
						double top = v.getPosition ().getY ();
						double bottom = v.getOtherCorner ().getY ();
						gl.glEnd ();
						gl.glBegin (GL2.GL_TRIANGLE_STRIP);
						{
							gl.glVertex2d (right, bottom);
							gl.glVertex2d (right, top);
							gl.glVertex2d (left, top);
							gl.glVertex2d (left, bottom);
							gl.glVertex2d (right, bottom);
						}
						gl.glEnd ();
						gl.glBegin (GL2.GL_LINE_STRIP);
					}
					else
					/* draw the next point in the turtle's path */
					{
						gl.glVertex2d (v.getPosition ().getX (), v.getPosition ().getY ());
					}
				}
				else
				/* if the pen is up, we need to put a break in the line */
				{
					gl.glEnd ();
					gl.glBegin (GL2.GL_LINE_STRIP);
				}
			}
		}
		gl.glEnd ();
	}

	/**
	 * Add a new turtle state to the sequence by moving the turtle forward
	 * along its current heading
	 * 
	 * @param distance
	 *            to move forward (in pixels)
	 */
	public void forward (int distance)
	{
		turtle = new TurtleState (new Vertex (turtle.getPosition ().getX () + (double) (distance * Math.cos (Math.toRadians (turtle.getHeading ()))), turtle.getPosition ().getY ()
			+ (double) (distance * Math.sin (Math.toRadians (turtle.getHeading ())))), turtle.getHeading (), turtle.getPenDown (), turtle.getPenColor ());
		vertices.add (turtle);
	}

	/**
	 * Add a new turtle state to the sequence by moving the turtle backward
	 * away from its current heading (but leaving its heading unchanged)
	 * 
	 * @param distance
	 *            to move backward (in pixels)
	 */
	public void back (int distance)
	{
		forward (distance * -1);
	}

	/**
	 * Add a new turtle state to the sequence by turning the turtle to the
	 * left while leaving it at its present position
	 * 
	 * @param degrees
	 *            to turn to the left (counter-clockwise)
	 */
	public void turnLeft (int degrees)
	{
		turtle = new TurtleState (turtle.getPosition (), turtle.getHeading () + degrees, turtle.getPenDown (), turtle.getPenColor ());
		vertices.add (turtle);
	}

	/**
	 * Add a new turtle state to the sequence by turning the turtle to the
	 * right while leaving it at its present position
	 * 
	 * @param degrees
	 *            to turn to the left (counter-clockwise)
	 */
	public void turnRight (int degrees)
	{
		turnLeft (degrees * -1);
	}

	/**
	 * Add a new turtle state to the sequence by instructing the turtle to
	 * pick up its pen (leave no marks when it moves), while leaving the
	 * turtle at its present position (this effectively ends a line
	 * segment)
	 */
	public void penUp ()
	{
		turtle = new TurtleState (turtle.getPosition (), turtle.getHeading (), false, turtle.getPenColor ());
		vertices.add (turtle);
	}

	/**
	 * Add a new turtle state to the sequence by instructing the turtle to
	 * pick up its pen (start leaving marks when it moves), while leaving
	 * th turtle at its present position (this effectively starts a new
	 * line segment)
	 */
	public void penDown ()
	{
		turtle = new TurtleState (turtle.getPosition (), turtle.getHeading (), true, turtle.getPenColor ());
		vertices.add (turtle);
	}

	/**
	 * Add a new turtle state to the sequence by instructing the turtle to
	 * change its pen color (whether it is up or down), while leaving it at
	 * its present position
	 * 
	 * @param red
	 *            component of the color [0-255]
	 * @param green
	 *            component of the color [0-255]
	 * @param blue
	 *            component of the color [0-255]
	 * @see <a href="http://en.wikipedia.org/wiki/Rgb">RGB Color Model</a>
	 * @see <a
	 *      href="http://id.mind.net/~zona/mmts/miscellaneousMath/intervalNotation/intervalNotation.html"
	 *      >Interval Notation</a>
	 */
	public void penColor (int red, int green, int blue)
	{
		turtle = new TurtleState (turtle.getPosition (), turtle.getHeading (), turtle.getPenDown (), new Color ((double) red / 255f, (double) green / 255f, (double) blue / 255f));
		vertices.add (turtle);
	}

	/**
	 * Change the current background color. This does not add a turtle
	 * state to the sequence (and therefore is not part of the undo
	 * sequence)
	 * 
	 * @param red
	 *            component of the color [0-255]
	 * @param green
	 *            component of the color [0-255]
	 * @param blue
	 *            component of the color [0-255]
	 * @see <a href="http://en.wikipedia.org/wiki/Rgb">RGB Color Model</a>
	 * @see <a
	 *      href="http://id.mind.net/~zona/mmts/miscellaneousMath/intervalNotation/intervalNotation.html"
	 *      >Interval Notation</a>
	 */
	public void background (int red, int green, int blue)
	{
		background = new Color ((double) red / 255f, (double) green / 255f, (double) blue / 255f);
	}

	/**
	 * Discard the current sequence of turtle states, restoring the drawing
	 * environment to its initial blank state. This also resets the
	 * background color to Black.
	 */
	public void reset ()
	{
		turtle = new TurtleState ();
		vertices = new ArrayList<TurtleState> ();
		vertices.add (turtle);
		background (0, 0, 0);
	}

	/**
	 * Remove a the most recent turtle state from the sequence, effectively
	 * undoing the last turtle instruction
	 */
	public void undo ()
	{
		vertices.remove (turtle);
		if (!vertices.isEmpty ())
		{
			turtle = vertices.get (vertices.size () - 1);
		}
		else
		{
			reset ();
			shell.println ("No more undos available");
		}
	}

	/**
	 * Add a turtle state to the sequence by instructing the turtle to move
	 * to a specific position on the screen (pen up or down determines
	 * whether or not a line is drawn from the present position to the new
	 * position)
	 * 
	 * @param x
	 *            coordinate of the new location (in pixels, relative to
	 *            the origin at the center of the drawing environment)
	 * @param y
	 *            coordinate of the new location (in pixels, relative to
	 *            the origin at the center of the drawing environment)
	 */
	public void move (int x, int y)
	{
		turtle = new TurtleState (new Vertex (x, y), turtle.getHeading (), turtle.getPenDown (), turtle.getPenColor ());
		vertices.add (turtle);
	}

	/**
	 * Included for compatibility with early versions of the Logo
	 * environment in which the heading change did not get added as a
	 * separate turtle state.
	 * 
	 * @deprecated
	 * @param degrees
	 *            specific heading for the turtle to face (in degrees, 0
	 *            degrees is due East, 90 due North)
	 * @see #heading(int)
	 */
	public void _heading (int degrees)
	{
		turtle = new TurtleState (turtle.getPosition (), degrees, turtle.getPenDown (), turtle.getPenColor ());
	}

	/**
	 * Add a turtle state to the sequence by instructing the turtle to
	 * change its heading to a specific direction
	 * 
	 * @param degrees
	 *            specific heading for the turtle to face (in degrees, 0
	 *            degrees is due East, 90 due North)
	 */
	public void heading (int degrees)
	{
		turtle = new TurtleState (turtle.getPosition (), degrees, turtle.getPenDown (), turtle.getPenColor ());
		vertices.add (turtle);
	}

	/**
	 * Add a turtle state to the sequence by instructing the turtle to draw
	 * a rectangle anchored at the present turtle position by its top-left
	 * corner
	 * 
	 * @param width
	 *            of the rectangle (in pixels)
	 * @param height
	 *            of the rectangle (in pixels)
	 */
	public void rectangle (int width, int height)
	{
		double left = Math.min (turtle.getPosition ().getX (), turtle.getPosition ().getX () + width);
		double right = Math.max (turtle.getPosition ().getX (), turtle.getPosition ().getX () + width);
		double bottom = Math.min (turtle.getPosition ().getY (), turtle.getPosition ().getY () - height);
		double top = Math.max (turtle.getPosition ().getY (), turtle.getPosition ().getY () - height);
		turtle = new TurtleState (new Vertex (left, top), new Vertex (right, bottom), turtle.getHeading (), turtle.getPenDown (), turtle.getPenColor ());
		vertices.add (turtle);
	}

	/**
	 * Display a standard error response to a malformed instruction in the
	 * shell
	 * 
	 * @param t
	 *            the event generating the error
	 * @param e
	 *            the exception details about the error
	 */
	private void error (ShellEvent t, Exception e)
	{
		shell.println (t + " not understood: " + e);
	}

	/**
	 * Handle a commandComplete event from the SimplerJOGL shell,
	 * processing the user's instructions to the turtle
	 */
	public void commandComplete (ShellEvent e)
	{
		/* have we parsed the command successfully? not yet! */
		boolean success = false;

		/* compare the current command to our vocabulary list */
		for (int i = 0; i < commands.length; i++ )
		{
			if (e.getCommand ().equals (commands[i]))
			{
				/*
				 * we have matched a command: now determine which one and
				 * send instructions to the turtle. However, even if we
				 * recognize the command, it may still have been malformed
				 * and generate an error.
				 */
				success = true;
				switch (i)
				{
					case MOVE:
						try
						{
							move (Integer.valueOf (e.getParameters ()[0]), Integer.valueOf (e.getParameters ()[1]));
						}
						catch (Exception ex)
						{
							error (e, ex);
							success = false;
						}
						break;
					case HEADING:
						try
						{
							heading (Integer.valueOf (e.getParameters ()[0]));
						}
						catch (Exception ex)
						{
							error (e, ex);
							success = false;
						}
						break;
					case FORWARD:
						try
						{
							forward (Integer.valueOf (e.getParameters ()[0]));
						}
						catch (Exception ex)
						{
							error (e, ex);
							success = false;
						}
						break;
					case BACK:
						try
						{
							back (Integer.valueOf (e.getParameters ()[0]));
						}
						catch (Exception ex)
						{
							error (e, ex);
							success = false;
						}
						break;
					case TURN_LEFT:
						try
						{
							turnLeft (Integer.valueOf (e.getParameters ()[0]));
						}
						catch (Exception ex)
						{
							error (e, ex);
							success = false;
						}
						break;
					case TURN_RIGHT:
						try
						{
							turnRight (Integer.valueOf (e.getParameters ()[0]));
						}
						catch (Exception ex)
						{
							error (e, ex);
							success = false;
						}
						break;
					case PEN_UP:
						penUp ();
						break;
					case PEN_DOWN:
						penDown ();
						break;
					case PEN_COLOR:
						try
						{
							penColor (Integer.valueOf (e.getParameters ()[0]), Integer.valueOf (e.getParameters ()[1]), Integer.valueOf (e.getParameters ()[2]));
						}
						catch (Exception ex)
						{
							error (e, ex);
							success = false;
						}
						break;
					case BACKGROUND:
						try
						{
							background (Integer.valueOf (e.getParameters ()[0]), Integer.valueOf (e.getParameters ()[1]), Integer.valueOf (e.getParameters ()[2]));
						}
						catch (Exception ex)
						{
							error (e, ex);
							success = false;
						}
						break;
					case RESET:
						reset ();
						break;
					case UNDO:
						undo ();
						break;
					case RECTANGLE:
						try
						{
							rectangle (Integer.valueOf (e.getParameters ()[0]), Integer.valueOf (e.getParameters ()[1]));
						}
						catch (Exception ex)
						{
							error (e, ex);
							success = false;
						}
						break;
				}
			}
		}

		/*
		 * if the command was not recognized (different from malformed)
		 * give the user an error response
		 */
		if (!success)
		{
			shell.println ("\"" + e.toString () + "\" not recognized");
		}

		/* await the next command to the shell */
		shell.readln ();
	}
}