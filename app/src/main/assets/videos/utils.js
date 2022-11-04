async function downloadVideo(url) {
    const res = await fetch(`/api/videos?q=${url}`);
    const json = await res.json();
    console.log(json);
    if (json.code === -1) {
        return null;
    }
    const video = {
        url,
        title: json.data.title,
        play: `https://www.tikwm.com/${json.data.hdplay}`,
        music_play: json.data.music_info.play,
        music_title: json.data.music_info.title,
        music_author: json.data.music_info.author,
        cover: `https://www.tikwm.com/${json.data.cover}`,
    };
    return video;
}