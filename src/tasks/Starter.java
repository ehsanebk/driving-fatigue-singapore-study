package tasks;

import java.io.File;
import actr.env.Core;

public class Starter implements actr.env.Starter {

	@Override
	public void startup(Core core) {
		core.openFrame(new File("model/DrivingDayA.actr"));
	}
}
