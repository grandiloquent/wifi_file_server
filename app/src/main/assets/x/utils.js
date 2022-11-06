async function downloadVideo(url) {
    const res = await fetch(`/v?q=${url}`);
    const json = await res.json();
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


async function saveVideo(url) {
    console.log(url);
    const video = await downloadVideo(url);

}