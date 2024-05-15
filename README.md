# ghidraScript
ghidraのRust用デコンパイラをつくろう&ghidraをもっと使いやすくするためのプラグインをつくろうプロジェクト
## TestScript
カーソル位置の関数を取得・CALL命令を再帰的に追跡し，ディスアセンブル結果をファイルに出力するスクリプト．実行例
```
main,00108920
PUSH R15
PUSH R14
PUSH R12
PUSH RBX
SUB RSP,0x58
MOV dword ptr [RSP + 0xc],0x7b
LEA RAX,[0x1553a8]
MOV qword ptr [RSP + 0x18],RAX
MOV qword ptr [RSP + 0x20],0x1
LEA RAX,[0x146000]
MOV qword ptr [RSP + 0x28],RAX
XORPS XMM0,XMM0
MOVUPS xmmword ptr [RSP + 0x30],XMM0
LEA RBX,[0x120ee0]
LEA RDI,[RSP + 0x18]
CALL RBX
  _print,00120ee0
  PUSH RBX
  SUB RSP,0x70
  MOV RBX,RDI
  LEA RAX,[0x14849a]
  MOV qword ptr [RSP + 0x10],RAX
  MOV qword ptr [RSP + 0x18],0x6
  CALL 0x00120c80
    print_to_buffer_if_capture_used,00120c80
    PUSH RBP
    PUSH R15
    PUSH R14
    PUSH R13
    PUSH R12
    PUSH RBX
    SUB RSP,0x18
    MOVZX EAX,byte ptr [0x00158068]
    TEST AL,AL
    JZ 0x00120d73
    MOV R15,RDI
    MOV RAX,qword ptr FS:[0x0]
    CMP qword ptr [RAX + -0x18],0x0
    JZ 0x00120cbf
    LEA R14,[RAX + -0x10]
    JMP 0x00120cd4
    XOR EBX,EBX
    XOR EDI,EDI
    CALL 0x00125cd0
      try_initialize,00125cd0
      PUSH R15
(省略)
```

### 現状見つけたバグ
再帰関数で実装したのでスタックオーバーフローする

## ExportDisas
TestScriptをベースに作成．解析したい関数，それに関連する関数名とアセンブリをjson形式でファイルに出力．以下出力例</br>
現在あるバグ
- 関数のエクスポート漏れ
```
[
    {
        "instruction": [
            "PUSH R15",
            "PUSH R14",
            "PUSH R12",
            "PUSH RBX",
            "SUB RSP,0x58",
            "MOV dword ptr [RSP + 0xc],0x7b",
            "LEA RAX,[0x1553a8]",
            "MOV qword ptr [RSP + 0x18],RAX",
            "MOV qword ptr [RSP + 0x20],0x1",
            "LEA RAX,[0x146000]",
            "MOV qword ptr [RSP + 0x28],RAX",
            "XORPS XMM0,XMM0",
            "MOVUPS xmmword ptr [RSP + 0x30],XMM0",
            "LEA RBX,[0x120ee0]",
            "LEA RDI,[RSP + 0x18]",
            "CALL RBX",
            "MOV dword ptr [RSP + 0x10],0x1c9",
            "MOV dword ptr [RSP + 0x14],0x1c9",
            "LEA RAX,[RSP + 0xc]",
            "MOV qword ptr [RSP + 0x48],RAX",
            "LEA R14,[0x144fc0]",
            "MOV qword ptr [RSP + 0x50],R14",
            "LEA R15,[0x1553b8]",
            "MOV qword ptr [RSP + 0x18],R15",
            "MOV qword ptr [RSP + 0x20],0x2",
            "MOV qword ptr [RSP + 0x38],0x0",
            "LEA R12,[RSP + 0x48]",
            "MOV qword ptr [RSP + 0x28],R12",
            "MOV qword ptr [RSP + 0x30],0x1",
            "LEA RDI,[RSP + 0x18]",
            "CALL RBX",
            "LEA RAX,[RSP + 0x10]",
            "MOV qword ptr [RSP + 0x48],RAX",
            "MOV qword ptr [RSP + 0x50],R14",
            "MOV qword ptr [RSP + 0x18],R15",
            "MOV qword ptr [RSP + 0x20],0x2",
            "MOV qword ptr [RSP + 0x38],0x0",
            "MOV qword ptr [RSP + 0x28],R12",
            "MOV qword ptr [RSP + 0x30],0x1",
            "LEA RDI,[RSP + 0x18]",
            "CALL RBX",
            "LEA RAX,[RSP + 0x14]",
            "MOV qword ptr [RSP + 0x48],RAX",
            "MOV qword ptr [RSP + 0x50],R14",
            "MOV qword ptr [RSP + 0x18],R15",
            "MOV qword ptr [RSP + 0x20],0x2",
            "MOV qword ptr [RSP + 0x38],0x0",
            "MOV qword ptr [RSP + 0x28],R12",
            "MOV qword ptr [RSP + 0x30],0x1",
            "LEA RDI,[RSP + 0x18]",
            "CALL RBX",
            "ADD RSP,0x58",
            "POP RBX",
            "POP R12",
            "POP R14",
            "POP R15",
            "RET"
        ],
        "function_name": "main"
    },

```
- データベースを作って関数名を自動でつけたい．
- オーダー的にハッシュ値を使うことになりそう
  - 使うならファジーハッシュ？
  - CALL命令のオペランドの重要度は高そう
## 他やりたいこと
- IDAのLuminaサーバーみたいなことをやりたい
- GPTを使ってデコンパイル結果の変数や関数のリネーム
- ソースコードとビルドレシピをアップロードしてそれに対応するバイナリを生成し学習．またそれを行うためのサーバーを作る．
# RenameFunc
Exportしたjsonを読み込みアセンブリが一致する関数をリネームする．
