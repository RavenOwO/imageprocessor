document.addEventListener("DOMContentLoaded", function () {
  const form = document.getElementById("uploadForm");
  const operationSelect = document.getElementById("operation");
  const paramsDiv = document.getElementById("params");
  const resultContainer = document.getElementById("resultContainer");
  const jobIdOutput = document.getElementById("jobIdOutput");

  operationSelect.addEventListener("change", () => {
    const op = operationSelect.value;
    paramsDiv.innerHTML = "";

    if (op === "resize") {
      paramsDiv.innerHTML = `
        <label>Width: <input type="number" id="width" value="300"></label>
        <label>Height: <input type="number" id="height" value="300"></label>
      `;
    } else if (op === "rotate") {
      paramsDiv.innerHTML = `
        <label>Angle: <input type="number" id="angle" value="90"></label>
      `;
    }
  });

  form.addEventListener("submit", async function (e) {
    e.preventDefault();

    const imageFile = document.getElementById("image").files[0];
    const operation = operationSelect.value;
    const width = document.getElementById("width")?.value;
    const height = document.getElementById("height")?.value;
    const angle = document.getElementById("angle")?.value;

    const formData = new FormData();
    formData.append("image", imageFile);

    let url = `/api/upload?operation=${operation}`;
    if (width) url += `&width=${width}`;
    if (height) url += `&height=${height}`;
    if (angle) url += `&angle=${angle}`;

    jobIdOutput.innerText = "Uploading...";
    resultContainer.innerHTML = "";

    try {
      const res = await fetch(url, {
        method: "POST",
        body: formData
      });

      const text = await res.text();
      const jobId = text.split(": ").pop().trim();
      jobIdOutput.innerText = `Job submitted. Job ID: ${jobId}`;

      // Poll for result
    const pollStatus = async () => {
      const statusRes = await fetch(`/api/status/${jobId}`);
      const statusJson = await statusRes.json();

      if (statusJson.state === "COMPLETED") {
        const imgBlob = await fetch(`/api/result/${jobId}`).then(r => r.blob());
        const imgUrl = URL.createObjectURL(imgBlob);

        resultContainer.innerHTML = `
          <h3>Result:</h3>
          <img src="${imgUrl}" class="preview"><br>
          <a href="${imgUrl}" download="processed-image.jpg">
            <button class="download-btn">Download Result</button>
          </a>
        `;
      } else if (statusJson.state === "FAILED") {
        resultContainer.innerHTML = `<p class="error">Processing failed.</p>`;
      } else {
        setTimeout(pollStatus, 1000);
      }
    };

      pollStatus();
    } catch (error) {
      jobIdOutput.innerText = "Upload failed.";
      console.error(error);
    }
  });
});