# ghidraScript
ghidraのRust用デコンパイラをつくろうプロジェクト
## TestScript
カーソル位置の関数を取得・CALL命令を再帰的に追跡し，ディスアセンブル結果をファイルに出力するスクリプト

### 現状見つけたバグ
再帰関数で実装したのでスタックオーバーフローする

## ExportDisas
TestScriptをベースに作成．解析したい関数，それに関連する関数名とアセンブリをファイルに出力．以下出力例
```
main,00108920
PUSH R15
PUSH R14
PUSH R12
PUSH RBX
SUB RSP,0x58
MOV dword ptr [RSP + 0xc],0x7b
(省略)
CALL RBX
ADD RSP,0x58
POP RBX
POP R12
POP R14
POP R15
RET

_print,00120ee0
PUSH RBX
SUB RSP,0x70
MOV RBX,RDI
LEA RAX,[0x14849a]
(省略)
LEA RSI,[0x155ba0]
LEA RDI,[RSP + 0x20]
CALL qword ptr [0x00157d50]
UD2

```
- データベースを作って関数名を自動でつけたい．
- オーダー的にハッシュ値を使うことになりそう
  - 使うならファジーハッシュ？
  - CALL命令のオペランドの重要度は高そう
