async function saveVideo(url) {
    const res = await fetch(`/v?q=${url}`);
    await res.text();
}