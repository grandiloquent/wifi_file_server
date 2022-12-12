

const mode = searchParams.get("m");
const p = searchParams.get('p');




async function saveData() {
    await submitData();
    document.getElementById('toast').setAttribute('message', '成功');
}


function insertString(editor, str) {

    var selection = editor.getSelection();

    if (selection.length > 0) {
        editor.replaceSelection(str);
    } else {

        var doc = editor.getDoc();
        var cursor = doc.getCursor();

        var pos = {
            line: cursor.line,
            ch: cursor.ch
        }

        doc.replaceRange(str, pos);

    }

}

