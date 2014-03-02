package io.github.chrisbotcom.reminder;

public class ReminderException extends Exception 
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ReminderException ()
        {
        }

    public ReminderException (String message)
        {
        super (message);
        }

    public ReminderException (Throwable cause)
        {
        super (cause);
        }

    public ReminderException (String message, Throwable cause)
        {
        super (message, cause);
        }
}
