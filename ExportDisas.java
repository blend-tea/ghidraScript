//
//@author blend-tea
//@category Export
//@keybinding
//@menupath
//@toolbar

import java.util.*;
import java.io.*;


import docking.widgets.filechooser.GhidraFileChooser;
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Program;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionIterator;
import ghidra.program.model.listing.Instruction;

public class ExportDisas extends GhidraScript {
	Set<String> written = new HashSet<String>();
	Set<String> notWritten = new HashSet<String>();
	Map<String, Address> funcs = new HashMap<String, Address>();
	
	
	@Override
	protected void run() throws Exception {
		//TODO: Add script code here
		GhidraFileChooser fileChooser = new GhidraFileChooser(null);
		fileChooser.setTitle("Save Disassembly");
		
		File selectedFile = fileChooser.getSelectedFile();
		if(selectedFile == null) {
			println("No file selected.");
			return;
		}
		
		String outputPath = selectedFile.getAbsolutePath();
		println(outputPath);
		
		PrintWriter pwriter = new PrintWriter(new BufferedWriter(new FileWriter(outputPath)));
		try {
			Address pl = currentLocation.getAddress();
			Program program = currentProgram;
			FunctionIterator funcIter = program.getFunctionManager().getFunctions(true);
			while (funcIter.hasNext()) {
				Function func = funcIter.next();
				funcs.put(func.getName(), func.getEntryPoint());
			}
			Function function = program.getFunctionManager().getFunctionContaining(pl);
			if (function != null) {
				Address[] startAddr = {function.getEntryPoint()};
				exportAsm(startAddr, pwriter);
			} else {
				println("No function found at the current location.");
			}
		} finally {
			pwriter.close();
		}
	}
	
	private void exportAsm(Address[] funcAddrs, PrintWriter pwriter) throws Exception {
		ArrayList<Address> writeList = new ArrayList<Address>();
		for (Address funcAddr : funcAddrs) {
			Function func = getFunctionAt(funcAddr);
			if (func != null && !written.contains(func.getName())) {
				written.add(func.getName());
				pwriter.println(func.getName() + "," + func.getEntryPoint());
				println(func.getName() + "," + func.getEntryPoint());
				Address endAddr = func.getBody().getMaxAddress();
				for(Address addr = funcAddr; addr.compareTo(endAddr) <= 0; addr = getInstructionAfter(addr).getAddress()) {
					Instruction inst = getInstructionAt(addr);
					if(inst == null) break;
					pwriter.println(inst.toString());
					println(inst.toString());
					if(inst.getMnemonicString().equals("CALL")) {
						Reference ref[]  = inst.getOperandReferences(0);
						if(ref.length != 0) {
							Address callAddr = ref[0].getToAddress();
							Function callFunc = getFunctionAt(callAddr);
							if (callFunc != null) {
								String FuncName = callFunc.getName();
								if (!written.contains(FuncName)) {
									writeList.add(callAddr);
								}
							}
						}
					}
				}
				pwriter.println("");
				println("");
			}
		}
		if (writeList.size() > 0) {
			exportAsm(writeList.toArray(new Address[writeList.size()]), pwriter);
		}
	}
}
