package org.biomart.dino.command;

import org.apache.commons.lang.StringUtils;

public class HypgCommand implements ShellCommand {

	String background, sets, annotations, cutoff, cmd;
	
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
	
	public HypgCommand setCmdBinPath(String bin) {
		this.cmd = bin;
		return this;
	}

	public String getBackground() {
		return background;
	}

	public HypgCommand setBackground(String background) {
		this.background = background;
		return this;
	}

	public String getSets() {
		return sets;
	}

	public HypgCommand setSets(String sets) {
		this.sets = sets;
		return this;
	}

	public String getAnnotations() {
		return annotations;
	}

	public HypgCommand setAnnotations(String annotations) {
		this.annotations = annotations;
		return this;
	}

	public String getCutoff() {
		return cutoff;
	}

	public HypgCommand setCutoff(String cutoff) {
		this.cutoff = cutoff;
		return this;
	}
	
	

}
