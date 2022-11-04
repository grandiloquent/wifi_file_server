async function downloadVideo(url) {
    const res = await fetch(`/api/videos?q=${url}`);
    const json = await res.json();
    console.log(json);
    if (json.code === -1) {
        return null;
    }
    const video = {
        url,
        title: (json.data && json.data.title) || json.title,
        play: (json.data && `https://www.tikwm.com/${json.data.hdplay}`) || json.play,
        music_play: (json.data && json.data.music_info.play) || '',
        music_title: (json.data && json.data.music_info.title) || '',
        music_author: (json.data && json.data.music_info.author) || '',
        cover: (json.data && `https://www.tikwm.com/${json.data.cover}`) || json.cover,
    };
    return video;
}

function exportToJsonString(idbDatabase, cb) {
    const exportObject = {};
    const objectStoreNamesSet = new Set(idbDatabase.objectStoreNames);
    const size = objectStoreNamesSet.size;
    if (size === 0) {
        console.log("xx");
        cb(null, JSON.stringify(exportObject));
    } else {
        const objectStoreNames = Array.from(objectStoreNamesSet);
        const transaction = idbDatabase.transaction(
            objectStoreNames,
            'readonly'
        );
        transaction.onerror = (event) => cb(event, null);

        objectStoreNames.forEach((storeName) => {
            const allObjects = [];
            transaction.objectStore(storeName).openCursor().onsuccess = (event) => {
                const cursor = event.target.result;
                if (cursor) {
                    allObjects.push(cursor.value);
                    cursor.continue();
                } else {
                    exportObject[storeName] = allObjects;
                    if (
                        objectStoreNames.length ===
                        Object.keys(exportObject).length
                    ) {
                        cb(null, JSON.stringify(exportObject));
                    }
                }
            };
        });
    }
}

function importFromJsonString(idbDatabase, jsonString, cb) {
    const objectStoreNamesSet = new Set(idbDatabase.objectStoreNames);
    const size = objectStoreNamesSet.size;
    if (size === 0) {
        cb(null);
    } else {
        const objectStoreNames = Array.from(objectStoreNamesSet);
        const transaction = idbDatabase.transaction(
            objectStoreNames,
            'readwrite'
        );
        transaction.onerror = (event) => cb(event);

        const importObject = JSON.parse(jsonString);

        // Delete keys present in JSON that are not present in database
        Object.keys(importObject).forEach((storeName) => {
            if (!objectStoreNames.includes(storeName)) {
                delete importObject[storeName];
            }
        });

        if (Object.keys(importObject).length === 0) {
            // no object stores exist to import for
            cb(null);
        }

        objectStoreNames.forEach((storeName) => {
            let count = 0;

            const aux = Array.from(importObject[storeName] || []);

            if (importObject[storeName] && aux.length > 0) {
                aux.forEach((toAdd) => {
                    const request = transaction.objectStore(storeName).add(toAdd);
                    request.onsuccess = () => {
                        count++;
                        if (count === importObject[storeName].length) {
                            // added all objects for this store
                            delete importObject[storeName];
                            if (Object.keys(importObject).length === 0) {
                                // added all object stores
                                cb(null);
                            }
                        }
                    };
                    request.onerror = (event) => {
                        console.log(event);
                    };
                });
            } else {
                if (importObject[storeName]) {
                    delete importObject[storeName];
                    if (Object.keys(importObject).length === 0) {
                        // added all object stores
                        cb(null);
                    }
                }
            }
        });
    }
}

function clearDatabase(idbDatabase, cb) {
    const objectStoreNamesSet = new Set(idbDatabase.objectStoreNames);
    const size = objectStoreNamesSet.size;
    if (size === 0) {
        cb(null);
    } else {
        const objectStoreNames = Array.from(objectStoreNamesSet);
        const transaction = idbDatabase.transaction(
            objectStoreNames,
            'readwrite'
        );
        transaction.onerror = (event) => cb(event);

        let count = 0;
        objectStoreNames.forEach(function (storeName) {
            transaction.objectStore(storeName).clear().onsuccess = () => {
                count++;
                if (count === size) {
                    // cleared all object stores
                    cb(null);
                }
            };
        });
    }
}
