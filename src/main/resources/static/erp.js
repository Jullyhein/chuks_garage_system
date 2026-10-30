// Funções comuns das telas do ERP

function menu(ativo) {
  const itens = [
    ["index.html", "Início"], ["cliente.html", "Cliente"], ["atendimento.html", "Atendimento"],
    ["checklist.html", "Checklist"], ["orcamento.html", "Orçamento"]
  ];
  document.getElementById("topo").innerHTML =
    '<span class="marca">Chuk\'s Car Services</span>' +
    itens.map(([href, nome]) =>
      `<a href="${href}" class="${href === ativo ? "ativo" : ""}">${nome}</a>`).join("");
}

function msg(texto, tipo) {
  const el = document.getElementById("msg");
  el.textContent = texto || "";
  el.className = tipo || "";
}

// lê o JSON da resposta; se a resposta for texto puro devolve {texto}
async function lerResposta(r) {
  const t = await r.text();
  try { return JSON.parse(t); } catch { return { texto: t }; }
}

async function enviarJson(url, metodo, corpo) {
  const r = await fetch(url, { method: metodo, headers: { "Content-Type": "application/json" }, body: JSON.stringify(corpo) });
  const d = await lerResposta(r);
  if (!r.ok) throw new Error(d.erro || d.mensagem || d.message || "Erro " + r.status);
  return d;
}

const val = id => document.getElementById(id).value;
const set = (id, v) => { document.getElementById(id).value = v == null ? "" : v; };

// escapa texto antes de colocar em innerHTML
const esc = s => String(s == null ? "" : s).replace(/[&<>"']/g, c => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));

async function pegarJson(url) {
  const r = await fetch(url);
  if (!r.ok) throw new Error("Erro " + r.status);
  return r.json();
}
