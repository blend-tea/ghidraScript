//
//@author blend-tea
//@category Functions
//@keybinding
//@menupath
//@toolbar

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import docking.widgets.filechooser.GhidraFileChooser;
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.symbol.SourceType;
import ghidra.util.filechooser.ExtensionFileFilter;

public class RenameFunc extends GhidraScript {
	Set<String> renamedFunc = new HashSet<String>();
	Set<String> searchedFunc = new HashSet<String>();

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
			Address p1 = currentLocation.getAddress();
			Function function = currentProgram.getFunctionManager().getFunctionContaining(p1);
			if (function != null) {
				Address[] startAddr = { function.getEntryPoint() };
				renameFunc(startAddr, jsonArray);
			}
		} finally {

		}
	}
    private void renameFunc(Address[] funcAddrs, JSONArray jsonList) throws Exception {
        ArrayList<Address> writeList = new ArrayList<Address>();
        for (Address funcAddr : funcAddrs) {
            Function func = getFunctionAt(funcAddr);
            if (func != null) {
                JSONObject funcObj = new JSONObject();
                JSONArray instArray = new JSONArray();
                Address endAddr = func.getBody().getMaxAddress();
                for(Address addr = funcAddr; addr.compareTo(endAddr) <= 0; addr = getInstructionAfter(addr).getAddress()) {
                    Instruction inst = getInstructionAt(addr);
                    if(inst == null) break;
                    instArray.add(inst.toString());

                    if(inst.getMnemonicString().equals("CALL")) {
                        Reference ref[]  = inst.getOperandReferences(0);
                        if(ref.length != 0) {
                            Address callAddr = ref[0].getToAddress();
                            Function callFunc = getFunctionAt(callAddr);
                            if (callFunc != null) {
                                String FuncName = callFunc.getName();
								if (!searchedFunc.contains(FuncName)) {
									searchedFunc.add(FuncName);
									writeList.add(callAddr);
								}
                            }
                        }
                    }
                }
				for (Object obj : jsonList) {
					JSONObject jsonObj = (JSONObject) obj;
					if (!renamedFunc.contains(jsonObj.get("function_name").toString())) {
						if (judgeAsm((JSONArray) jsonObj.get("instruction"), instArray) > 0.6) {
							func.setName(jsonObj.get("function_name").toString(), SourceType.USER_DEFINED);
							println(jsonObj.get("function_name").toString());
							renamedFunc.add(jsonObj.get("function_name").toString());
							break;
						}
					}
				}
            }
        }
        if (writeList.size() > 0) {
            renameFunc(writeList.toArray(new Address[writeList.size()]), jsonList);
        }
}

	private double judgeAsm(JSONArray json1, JSONArray json2) throws Exception {
		int matchCount = 0;
		if(json1.size() != json2.size()) {
			return 0;
		}
	    for (int i = 0; i < json1.size(); i++) {
	        String str1 = (String) json1.get(i);
	        String str2 = (String) json2.get(i);
	        if (str1.equals(str2)) {
	            matchCount++;
	        }
	    }
		return (double)matchCount / json1.size();
		
    }
}
