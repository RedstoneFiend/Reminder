package io.github.chrisbotcom.reminder;

public enum Commands {
	add,
	list,
	delete,
	update,
	setdefault,
	reload,
	stop,
	resume,
	time,
	error;

		public static Commands lookup(String command) {
		Commands returnValue;
		try {
			returnValue = Commands.valueOf(command.toLowerCase());
		}
		catch (IllegalArgumentException e) {
			returnValue = Commands.error;
		}
		return returnValue;
	}
}