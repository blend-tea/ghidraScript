//
//@author blend-tea
//@category Export
//@keybinding
//@menupath
//@toolbar

import java.util.*;
import java.io.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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
        GhidraFileChooser fileChooser = new GhidraFileChooser(null);
        fileChooser.setTitle("Save Disassembly");

        File selectedFile = fileChooser.getSelectedFile();
        if(selectedFile == null) {
            println("No file selected.");
            return;
        }

        String outputPath = selectedFile.getAbsolutePath();
        println(outputPath);

        JSONArray jsonList = new JSONArray();

        try (PrintWriter pwriter = new PrintWriter(new BufferedWriter(new FileWriter(outputPath)))) {
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
                exportAsm(startAddr, jsonList);
            } else {
                println("No function found at the current location.");
            }

            // Write JSON to file
            pwriter.write(jsonList.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exportAsm(Address[] funcAddrs, JSONArray jsonList) throws Exception {
        ArrayList<Address> writeList = new ArrayList<Address>();
        for (Address funcAddr : funcAddrs) {
            Function func = getFunctionAt(funcAddr);
            if (func != null && !written.contains(func.getName())) {
                written.add(func.getName());

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
                                if (!written.contains(FuncName)) {
                                    writeList.add(callAddr);
                                }
                            }
                        }
                    }
                }
                funcObj.put("function_name", func.getName());
                funcObj.put("instruction", instArray);
                jsonList.add(funcObj);
            }
        }
        if (writeList.size() > 0) {
            exportAsm(writeList.toArray(new Address[writeList.size()]), jsonList);
        }
    }
}
