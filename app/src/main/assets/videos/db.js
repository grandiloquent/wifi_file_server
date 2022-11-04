const DB_NAME = "videos";

function openDatabase() {
    return new Promise((resolve, reject) => {
        const dbRequest = indexedDB.open(DB_NAME, 2);
        dbRequest.onsuccess = evt => {
            const db = dbRequest.result;
            if (!db.objectStoreNames.contains(DB_NAME)) {
                db.createObjectStore(DB_NAME, { keyPath: '_id' });
            }
            resolve(db);
        }
        dbRequest.onupgradeneeded = evt => {
            dbRequest.result.createObjectStore(DB_NAME, { keyPath: '_id' });
            console.log(dbRequest.result)
            resolve(dbRequest.result);
        }
    });
}

function addItem(db) {
    const transaction = db.transaction(['videos'], 'readwrite');
    const objectStore = transaction.objectStore('videos');
    const objectStoreRequest = objectStore.add({
        good: 1,
        _id: 4,
        save: 5
    });
}

function getKey(db) {
    return new Promise((resolve, reject) => {
        const transaction = db.transaction([DB_NAME], 'readonly');
        const objectStore = transaction.objectStore(DB_NAME);
        const req = objectStore.getAllKeys();
        req.onsuccess = (event) => {
            if (req.result.length) {
                return req.result.sort().reverse()[0] + 1;
            } else {
                return 1;
            }
        }
    });
}