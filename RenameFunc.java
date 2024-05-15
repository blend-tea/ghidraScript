//
//@author blend-tea
//@category Functions
//@keybinding
//@menupath
//@toolbar

import java.io.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import docking.widgets.filechooser.GhidraFileChooser;
import ghidra.app.script.GhidraScript;
import ghidra.util.filechooser.ExtensionFileFilter;

public class RenameFunc extends GhidraScript {

	@Override
	protected void run() throws Exception {
		GhidraFileChooser fileChooser = new GhidraFileChooser(null);
		fileChooser.setTitle("Choose Database File");
		fileChooser.setFileFilter(new ExtensionFileFilter("json", "JSON Files"));
		
		File selectedFile = fileChooser.getSelectedFile();
		if (selectedFile == null) {
			println("No file selected.");
			return;
		}
		
		String dataBasePath = selectedFile.getAbsolutePath();
		println(dataBasePath);
		try {
			JSONParser parser = new JSONParser();
			JSONArray jsonArray = (JSONArray) parser.parse(new FileReader(dataBasePath));
			for (Object obj : jsonArray) {
				JSONObject jsonObj = (JSONObject) obj;
				println(jsonObj.get("function_name").toString());
//				println(obj.toString());
			}
		} finally {

		}
	}
}
