package org.biomart.configurator.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.biomart.common.resources.Log;
import org.biomart.common.resources.Resources;
import org.biomart.common.resources.Settings;
import org.biomart.configurator.controller.MartController;
import org.biomart.configurator.utils.McGuiUtils;
import org.biomart.configurator.utils.McUtils;
import org.biomart.configurator.view.menu.McMenus;
import org.biomart.objects.objects.Column;
import org.biomart.objects.objects.Dataset;
import org.biomart.objects.objects.DatasetTable;
import org.biomart.objects.objects.Mart;
import org.biomart.objects.objects.MartRegistry;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

public class MergeCLI {
	
	private String originalRegistryFile;
	private String updatedRegistryFile;
	private String datasetRoot;
	
	private HashMap<String, HashMap<String, HashMap<String, ArrayList<String>>>> updatedInfo = new HashMap<String, HashMap<String,HashMap<String,ArrayList<String>>>>();
	
	public static void main(String[] args) {
		if(args.length<3) {
			System.exit(0);
		}
  		Resources.setResourceLocation("org/biomart/configurator/resources");  	
        Settings.loadGUIConfigProperties();
        Settings.loadAllConfigProperties();
        
        MergeCLI mergeCLI = new MergeCLI();
        
        mergeCLI.update(args[0], args[1], args[2]);
	}
	
	private void update(String originalRegistryFile, String updatedRegistryFile, String datasetRoot){
		this.originalRegistryFile = originalRegistryFile;
		this.updatedRegistryFile = updatedRegistryFile;
		this.datasetRoot = datasetRoot;

		this.loadUpdatedTables();

		MartRegistry originalRegistry = this.openXML(this.originalRegistryFile);



		for(Mart originalMart : originalRegistry.getMartList()){
			Dataset originalDS = originalMart.getDatasetBySuffix(this.datasetRoot);
			if(null != originalDS){
				String originalDSname = originalDS.getName();

				if(this.updatedInfo.containsKey(originalMart.getName())){
					HashMap<String, ArrayList<String>> updatedTables = this.updatedInfo.get(originalMart.getName()).get(originalDSname);

					for(DatasetTable originalTable : originalMart.getDatasetTables()){

						ArrayList<String> updatedColumns = updatedTables.get(originalTable.getName());

						if(null != updatedColumns){
							originalTable.addInPartitions(originalDSname);

							for(Column originalColumn : originalTable.getColumnList()){
								if(updatedColumns.contains(originalColumn.getName())){
									originalColumn.addInPartitions(originalDSname);
								} else {
									originalColumn.removeFromPartitions(originalDSname);
								}
							}
						} else {
							originalTable.removeFromPartition(originalDSname);
						}
					}
				}
			}
		}
		this.saveXML();
	}
	
	private void loadUpdatedTables(){
		MartRegistry updatedRegistry = this.openXML(this.updatedRegistryFile);
		
		for(Mart updatedMart : updatedRegistry.getMartList()){
			Dataset updatedDS = updatedMart.getDatasetBySuffix(this.datasetRoot);
			HashMap<String, HashMap<String, ArrayList<String>>> updatedDatasets = new HashMap<String, HashMap<String,ArrayList<String>>>();
			
			if(null != updatedDS){
				String dsName = updatedDS.getName();
				HashMap<String, ArrayList<String>> updatedTables = new HashMap<String, ArrayList<String>>();
				
				for(DatasetTable updatedTable : updatedMart.getDatasetTablesForDataset(dsName)){
					ArrayList<String> updatedColumns = new ArrayList<String>();
					
					for(Column updatedColumn : updatedTable.getColumnList(dsName)){
						updatedColumns.add(updatedColumn.getName());
					}
					
					updatedTables.put(updatedTable.getName(), updatedColumns);
				}
				
				updatedDatasets.put(dsName, updatedTables);
			}
			this.updatedInfo.put(updatedMart.getName(), updatedDatasets);
		}
	}
	
	private MartRegistry openXML(String registryFile) {
		MartRegistry registry = McGuiUtils.INSTANCE.getRegistryObject();
		File file = new File(registryFile);
		//set key file
		String tmpName = file.getName();
		int index = tmpName.lastIndexOf(".");
		if(index>0)
			tmpName = tmpName.substring(0,index);
		String keyFileName = file.getParent()+File.separator+"."+tmpName;
		BufferedReader input;
		String key=null;
		try {
			input = new BufferedReader(new FileReader(keyFileName));
			key = input.readLine();
			input.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Log.error("key file not found");
			//if key file no found generate one

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		McUtils.setKey(key);
		
		Document document = null;
		try {
			SAXBuilder saxBuilder = new SAXBuilder("org.apache.xerces.parsers.SAXParser", false);
			document = saxBuilder.build(file);
		}
		catch (Exception e) {
			e.printStackTrace();
			return registry;
		}
		
		MartController.getInstance().requestCreateRegistryFromXML(registry, document);
		return registry;
	}

	private String saveXML() {		
		int index = this.originalRegistryFile.lastIndexOf(".");
		String prefix = this.originalRegistryFile.substring(0, index);
		String savedFile = prefix + "-"+ McUtils.getCurrentTimeString()+".xml";
		File file = new File(savedFile);
		McMenus.getInstance().requestSavePortalToFile(file, false);
		return file.getAbsolutePath();
	}
}
