package it.infn.ct.GridEngine.JobCollection;

import it.infn.ct.GridEngine.JobResubmission.GEJobDescription;

import java.io.File;
import java.util.ArrayList;

public class JobParametric extends JobCollection {

	private String executable;
	
	public JobParametric(String commonName, String description,
			String outputPath, ArrayList<GEJobDescription> subJobDescriptions,
			String executable) {
		this(commonName, description, null, outputPath, subJobDescriptions, executable);
	}

	public JobParametric(String commonName, String description,
			String userEmail, String outputPath,
			ArrayList<GEJobDescription> subJobDescriptions, String executable) {
		File f = new File(executable);
		this.executable=f.getName();
		
		super.setCommonName(commonName);
		super.setDescription(description);
		super.setUserEmail(userEmail);
		super.setOutputPath(outputPath);
		for (GEJobDescription geJobDescription : subJobDescriptions) {
			geJobDescription.setExecutable(this.getExecutable());
			if(!geJobDescription.getInputFiles().contains(executable))
				geJobDescription.setInputFiles(geJobDescription.getInputFiles()+","+executable);
		}
		super.setSubJobDescriptions(subJobDescriptions);
		super.setTaskCounter(subJobDescriptions.size());
		
		saveJobCollection();
	}

	protected JobParametric(){
		super();
	}

	public String getExecutable() {
		return executable;
	}
	
}
