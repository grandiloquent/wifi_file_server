(() => {
    const strings = `1
    00:00:01,101 --> 00:00:03,737
    China's shopping extravaganza, Singles Day
    
    2
    00:00:03,737 --> 00:00:07,107
    did not match up to its usual high
    sales counts Friday.
    
    3
    00:00:07,574 --> 00:00:11,678
    One research firm said Alibaba
    and other Chinese e-commerce companies
    
    4
    00:00:11,678 --> 00:00:16,883
    saw a 4.7% fall in sales
    for the first 12 hours of the final day.
    
    5
    00:00:17,250 --> 00:00:20,920
    The number was in line with lower
    expectations for this year's event.
    
    6
    00:00:21,121 --> 00:00:24,257
    Analysts saw consumer sentiment
    at a low point due
    
    7
    00:00:24,257 --> 00:00:27,127
    to health crisis, curbs
    and a slowing economy.
    
    8
    00:00:27,327 --> 00:00:28,028
    Singles Day
    
    9
    00:00:28,028 --> 00:00:31,798
    is normally one of the high points
    of the year for China's e-commerce giants.
    
    10
    00:00:31,998 --> 00:00:32,599
    It's the world's
    
    11
    00:00:32,599 --> 00:00:36,569
    biggest online shopping festival
    and has become a multi-week event.
    
    12
    00:00:36,903 --> 00:00:39,572
    Alibaba usually has a major celebrity
    
    13
    00:00:39,572 --> 00:00:43,143
    to perform at its gala show,
    but this year that was not the case.
    
    14
    00:00:43,443 --> 00:00:46,212
    It's part of an effort
    to play down hype around the event.
    
    15
    00:00:46,479 --> 00:00:51,751
    As President Xi Jinping pushes his idea
    of common prosperity that aims to clamp
    
    16
    00:00:51,751 --> 00:00:55,789
    down on what the Communist Party sees
    as so-called excessive behaviors.
    
    17
    00:00:56,122 --> 00:00:59,225
    Analysts expect this will be the weakest
    ever year for growth
    
    18
    00:00:59,225 --> 00:01:01,795
    in gross merchandise
    value over Singles Day.
    
    19
    00:01:02,562 --> 00:01:07,333
    Analysts forecast Alibaba's
    GMV to reach up to $77 billion this year
    
    20
    00:01:07,400 --> 00:01:10,503
    or a rise of up to 3.6% as compared
    
    21
    00:01:10,503 --> 00:01:13,740
    to a much larger 26% jump two years ago.
    
    22
    00:01:14,007 --> 00:01:18,411
    Alibaba did not respond to requests
    for comments on their overall GMV outlook.
    
    23
    00:01:18,778 --> 00:01:22,115
    They are expected to give final numbers
    after midnight Friday.
    `;

    const lines = strings.split('\n');
    const buf = [];
    for (let index = 0; index < lines.length; index++) {
        const element = lines[index].trim();
        if (/^\d+$/.test(element)) {
            const match = /(\d{2}:\d{2}:\d{2},\d{3}) --> (\d{2}:\d{2}:\d{2},\d{3})/.exec(lines[index + 1]);
            buf.push({
                index: parseInt(element),
                start: match[1],
                end: match[2],
                string: lines[index + 2].trim()
            })
        }
    }
    console.log("----------------->",buf)
    const result = [];
    let offset = 1;
    let string = "";
    let start = "";
    let end = "";
    for (let index = 0; index < buf.length; index++) {
        const element = buf[index];
        if (element.string.endsWith('.') || element.string.endsWith('?')) {
            element.index = offset++;
            result.push(element);
        } else {
            start = element.start;
            string = element.string;
            for (let j = index + 1; j < buf.length; j++) {
                end = buf[j].end;
                string += " " + buf[j].string;
                if (buf[j].string.endsWith('.') || buf[j].string.endsWith('?')) {
                    index = j;
                    break;
                }
            }
            result.push({
                index: offset,
                start: start,
                end: end,
                string: string
            })
            offset++;

        }
    }
    console.log(result.map(x=>{
        return `${x.index}
${x.start} --> ${x.end}
${x.string}

`
    }).join(''));

})();