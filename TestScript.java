//Test Script
//@author blend-tea
//@category Test
//@keybinding
//@menupath
//@toolbar


import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.io.*;


import docking.widgets.filechooser.GhidraFileChooser;
import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Program;
import ghidra.program.model.symbol.Reference;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Instruction;

public class TestScript extends GhidraScript {

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
			Function function = program.getFunctionManager().getFunctionContaining(pl);
			if (function != null) {
				Address startAddr = function.getEntryPoint();
				search_func(startAddr, pwriter, 0);
			} else {
				println("No function found at the current location.");
			}
		} finally {
			pwriter.close();
		}
	}
	
	
	private void search_func(Address startAddr,PrintWriter pwriter, int c) throws Exception {
		Function func = getFunctionAt(startAddr);
		String indent = String.join("", Stream.generate(() -> "  ").limit(c).collect(Collectors.toList()));
		if(func != null) {
			pwriter.println(indent + func.getName());
			Address endAddr = func.getBody().getMaxAddress();
			for(Address addr = startAddr; addr.compareTo(endAddr) <= 0; addr = getInstructionAfter(addr).getAddress()) {
				Instruction inst = getInstructionAt(addr);
				if(inst == null) break;
				pwriter.println(indent + inst.toString());
				if(inst.getMnemonicString().equals("CALL")) {
//					println(indent + addr.toString());
					Reference ref[]  = inst.getOperandReferences(0);
					if(ref.length != 0) {
						Address callAddr = ref[0].getToAddress();
						search_func(callAddr, pwriter, c + 1);
					}
				}
			}
		}
	}
}
		
