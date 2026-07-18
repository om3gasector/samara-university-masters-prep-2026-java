(function () {
  const loading = document.getElementById("loading");
  const errorEl = document.getElementById("error");
  const cardEl = document.getElementById("card");
  const empty = document.getElementById("empty");
  const meta = document.getElementById("meta");
  const front = document.getElementById("front");
  const back = document.getElementById("back");
  const backWrap = document.getElementById("backWrap");
  const reveal = document.getElementById("reveal");
  const actions = document.getElementById("actions");
  const good = document.getElementById("good");
  const bad = document.getElementById("bad");
  const counter = document.getElementById("counter");
  const filterInput = document.getElementById("filter");
  const reloadBtn = document.getElementById("reload");
  const skillSelect = document.getElementById("skill");
  const subfolderSelect = document.getElementById("subfolder");

  const STORAGE_SKILL = "learn-app-skill";
  const STORAGE_SUB = "learn-app-subfolder";

  const ERR_SERVER =
    "Нет ответа от сервера. Запустите в папке learn-app команду python server.py и откройте в браузере именно http://127.0.0.1:8765 (не файл index.html и не только «localhost», если не открывается).";
  const ERR_FILE =
    "Страница открыта как локальный файл — запросы к API не работают. Запустите python server.py в learn-app и зайдите по адресу http://127.0.0.1:8765";

  let queue = [];
  let index = 0;

  if (typeof marked !== "undefined") {
    marked.setOptions({
      gfm: true,
      breaks: true,
    });
  }

  function shuffle(arr) {
    const a = arr.slice();
    for (let i = a.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [a[i], a[j]] = [a[j], a[i]];
    }
    return a;
  }

  function escapeHtml(s) {
    return s
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;");
  }

  function sanitize(html) {
    if (typeof DOMPurify !== "undefined") {
      return DOMPurify.sanitize(html, {
        ALLOWED_URI_REGEXP:
          /^(?:(?:https?|mailto):|[^a-z]|[a-z+.-]+(?:[^a-z+.\-:]|$))/i,
      });
    }
    return html;
  }

  function renderMarkdownBlock(text) {
    if (typeof marked === "undefined") {
      const lines = text.split("\n");
      return lines.map((line) => `<p>${escapeHtml(line) || "&nbsp;"}</p>`).join("");
    }
    return sanitize(marked.parse(text));
  }

  function renderMarkdownInline(text) {
    if (typeof marked === "undefined") {
      return `<span>${escapeHtml(text)}</span>`;
    }
    if (typeof marked.parseInline === "function") {
      return sanitize(marked.parseInline(text));
    }
    return sanitize(marked.parse(text.replace(/\n/g, " ")));
  }

  function renderBack(text) {
    back.innerHTML = renderMarkdownBlock(text);
  }

  function renderFront(text) {
    front.innerHTML = renderMarkdownInline(text);
  }

  function setError(msg) {
    if (!msg) {
      errorEl.classList.add("hidden");
      errorEl.textContent = "";
      return;
    }
    errorEl.textContent = msg;
    errorEl.classList.remove("hidden");
  }

  function setLoading(on) {
    loading.classList.toggle("hidden", !on);
  }

  function showCard() {
    reveal.classList.remove("hidden");
    backWrap.classList.add("hidden");
    actions.classList.add("hidden");

    if (!queue.length) {
      cardEl.classList.add("hidden");
      empty.classList.remove("hidden");
      counter.textContent = "";
      return;
    }

    empty.classList.add("hidden");
    cardEl.classList.remove("hidden");
    const c = queue[index];
    meta.textContent = c.source;
    renderFront(c.front);
    renderBack(c.back);
    counter.textContent = `Карточка ${index + 1} из ${queue.length}`;
  }

  function cardsUrl() {
    const params = new URLSearchParams();
    params.set("skill", skillSelect.value);
    const sub = subfolderSelect.value;
    if (sub) {
      params.set("subfolder", sub);
    }
    const q = filterInput.value.trim();
    if (q) {
      params.set("prefix", q);
    }
    return `/api/cards?${params.toString()}`;
  }

  async function parseJsonResponse(res) {
    const text = await res.text();
    try {
      return JSON.parse(text);
    } catch {
      return null;
    }
  }

  async function loadSkills() {
    const res = await fetch("/api/skills", { cache: "no-store" });
    const data = await parseJsonResponse(res);
    if (!res.ok || !data) {
      throw new Error("skills");
    }
    const skills = data.skills || [];
    skillSelect.innerHTML = "";
    for (const s of skills) {
      const opt = document.createElement("option");
      opt.value = s.id;
      opt.textContent = s.label;
      skillSelect.appendChild(opt);
    }
    if (!skills.length) {
      const opt = document.createElement("option");
      opt.value = "";
      opt.textContent = "— нет папок в «Знания» —";
      opt.disabled = true;
      skillSelect.appendChild(opt);
      skillSelect.disabled = true;
    } else {
      skillSelect.disabled = false;
      const savedSkill = localStorage.getItem(STORAGE_SKILL);
      if (savedSkill && [...skillSelect.options].some((o) => o.value === savedSkill)) {
        skillSelect.value = savedSkill;
      }
    }
  }

  function fillSubfolderOptions(subfolders, preferredId) {
    subfolderSelect.innerHTML = "";
    const all = document.createElement("option");
    all.value = "";
    all.textContent = "Весь навык";
    subfolderSelect.appendChild(all);
    for (const s of subfolders) {
      const opt = document.createElement("option");
      opt.value = s.id;
      opt.textContent = s.label;
      subfolderSelect.appendChild(opt);
    }
    if (preferredId && [...subfolderSelect.options].some((o) => o.value === preferredId)) {
      subfolderSelect.value = preferredId;
    }
  }

  async function loadSubfolders(preferredSub) {
    if (!skillSelect.value || skillSelect.disabled) {
      fillSubfolderOptions([], "");
      subfolderSelect.disabled = true;
      return;
    }
    subfolderSelect.disabled = false;
    const params = new URLSearchParams();
    params.set("skill", skillSelect.value);
    const res = await fetch(`/api/subfolders?${params.toString()}`, { cache: "no-store" });
    const data = await parseJsonResponse(res);
    if (!res.ok || !data) {
      throw new Error("subfolders");
    }
    const subs = data.subfolders || [];
    const saved = preferredSub !== undefined ? preferredSub : localStorage.getItem(STORAGE_SUB);
    fillSubfolderOptions(subs, saved || "");
  }

  async function loadDeck() {
    setLoading(true);
    cardEl.classList.add("hidden");
    empty.classList.add("hidden");
    setError("");
    try {
      if (!skillSelect.options.length || !skillSelect.value || skillSelect.disabled) {
        queue = [];
        setError("Нет папок в «Знания». Создайте подпапку с заметками в каталоге «Знания» хранилища.");
        showCard();
        return;
      }

      const res = await fetch(cardsUrl(), { cache: "no-store" });
      const data = await parseJsonResponse(res);

      if (!data) {
        queue = [];
        setError("Сервер вернул не JSON. Убедитесь, что запущен learn-app (python server.py), а не другой сервер на этом порту.");
        showCard();
        return;
      }

      if (!res.ok) {
        queue = [];
        setError(data.error || "Ошибка загрузки колоды.");
        showCard();
        return;
      }

      queue = shuffle(data.cards || []);
      index = 0;
      showCard();
    } catch (e) {
      console.error(e);
      queue = [];
      setError(ERR_SERVER);
      showCard();
    } finally {
      setLoading(false);
    }
  }

  reveal.addEventListener("click", () => {
    backWrap.classList.remove("hidden");
    actions.classList.remove("hidden");
    reveal.classList.add("hidden");
  });

  good.addEventListener("click", () => {
    index += 1;
    if (index >= queue.length) {
      queue = shuffle(queue);
      index = 0;
    }
    showCard();
  });

  bad.addEventListener("click", () => {
    const c = queue.splice(index, 1)[0];
    queue.push(c);
    if (index >= queue.length) index = 0;
    showCard();
  });

  reloadBtn.addEventListener("click", loadDeck);
  filterInput.addEventListener("keydown", (e) => {
    if (e.key === "Enter") loadDeck();
  });

  skillSelect.addEventListener("change", async () => {
    localStorage.setItem(STORAGE_SKILL, skillSelect.value);
    localStorage.removeItem(STORAGE_SUB);
    try {
      await loadSubfolders("");
    } catch (e) {
      console.error(e);
      setError(ERR_SERVER);
      return;
    }
    await loadDeck();
  });

  subfolderSelect.addEventListener("change", () => {
    localStorage.setItem(STORAGE_SUB, subfolderSelect.value);
    loadDeck();
  });

  async function init() {
    if (location.protocol === "file:") {
      setError(ERR_FILE);
      return;
    }
    setLoading(true);
    try {
      await loadSkills();
      await loadSubfolders();
    } catch (e) {
      console.error(e);
      setError(ERR_SERVER);
      skillSelect.disabled = true;
      subfolderSelect.disabled = true;
      return;
    } finally {
      setLoading(false);
    }
    await loadDeck();
  }

  init();
})();
