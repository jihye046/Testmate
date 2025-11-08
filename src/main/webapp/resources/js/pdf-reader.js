const axios = require("axios");
const pdf = require("pdf-parse"); // pdf-parse 라이브러리 불러옴

const pdfUrl = "https://www.gumsi.or.kr/ged/cmmn/brd/fileDownload.do?flId=1128&flSn=1";
// const pdfUrl1 = "https://www.kice.re.kr/boardCnts/fileDown.do"
// const fildSeq = "be5f5054d247c9396b8b1206b31f6025"

// Java 서버 API 주소
const javaApiUrl = "http://localhost:8080/exam/uploadPdfText";

function getPdfTextWithAxios(url) {
    axios({
        method: "get",
        // method: "post",
        url: url,
        responseType: "arraybuffer", // PDF 다운로드를 위한 바이너리 응답
        headers: {
            // "Content-Type": "application/x-www-form-urlencoded",
            "User-Agent":
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36",
        },
        // data: {
        //     fileSeq: fildSeq
        // }
    })
    .then((response) => {
        const buffer = Buffer.from(response.data);
        return pdf(buffer); // 다운로드한 pdf 텍스트 추출
    })
    .then((data) => {
        console.log("✅ PDF 텍스트 추출 성공!");
        console.log("----------------------------------");
        console.log(data.text);

        // Java 서버에 POST 요청으로 텍스트 전송
        return axios.post(javaApiUrl, { text: data.text });
    })
    .then((response) => {
        console.log("🚀 Java 서버 응답:", response.data);
    })
    .catch((error) => {
        console.error("❌ 오류 발생:", error.message);
        console.error(error.stack);
    });
}

// 실행
getPdfTextWithAxios(pdfUrl);