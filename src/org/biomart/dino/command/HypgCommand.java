package org.biomart.dino.command;

import org.apache.commons.lang.StringUtils;

public class HypgCommand implements Command {

	String background, sets, annotations, cutoff, cmd;
	
	public HypgCommand(String cmdBinPath) {
		this.cmd = cmdBinPath;
	}
	
	@Override
	public String build() {
		return StringUtils.join(new String[] {
			cmd,
			"-g", getBackground(),
			"-s", getSets(),
			"-a", getAnnotations(),
			"-c", getCutoff()
		}, " ");
	}
	
	public String getCmdBinPath() {
		return cmd;
	}

	public String getBackground() {
		return background;
	}

	public void setBackground(String background) {
		this.background = background;
	}

	public String getSets() {
		return sets;
	}

	public void setSets(String sets) {
		this.sets = sets;
	}

	public String getAnnotations() {
		return annotations;
	}

	public void setAnnotations(String annotations) {
		this.annotations = annotations;
	}

	public String getCutoff() {
		return cutoff;
	}

	public void setCutoff(String cutoff) {
		this.cutoff = cutoff;
	}
	
	

}
