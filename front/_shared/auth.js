// API base URL do backend Spring Boot
const API_BASE = 'http://localhost:8081/api';

// Chave usada no localStorage para guardar o token JWT
const TOKEN_KEY = 'moviewatch_token';

// Chave usada no localStorage para guardar os dados do usuário
const USUARIO_KEY = 'moviewatch_usuario';

// Salva o token no localStorage
function salvarToken(token) {
    localStorage.setItem(TOKEN_KEY, token);
}

// Recupera o token salvo
function getToken() {
    return localStorage.getItem(TOKEN_KEY);
}

// Remove o token e os dados do usuário (logout completo)
function removerToken() {
    localStorage.removeItem(TOKEN_KEY);
    removerUsuario();
}

// Salva os dados do usuário no localStorage
function salvarUsuario(usuario) {
    localStorage.setItem(USUARIO_KEY, JSON.stringify(usuario));
}

// Recupera os dados do usuário salvo
function getUsuario() {
    const raw = localStorage.getItem(USUARIO_KEY);
    if (!raw) return null;
    try {
        return JSON.parse(raw);
    } catch {
        return null;
    }
}

// Remove os dados do usuário (logout)
function removerUsuario() {
    localStorage.removeItem(USUARIO_KEY);
}

// Retorna headers com Authorization Bearer quando houver token
function authHeaders() {
    const token = getToken();
    const headers = {
        'Content-Type': 'application/json'
    };
    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }
    return headers;
}

// Faz fetch com headers de auth e trata resposta de erro
async function apiFetch(path, options = {}) {
    const url = API_BASE + path;
    const config = {
        ...options,
        headers: {
            ...authHeaders(),
            ...options.headers
        }
    };

    const resposta = await fetch(url, config);

    if (resposta.status === 401) {
        removerToken();
        window.location.href = '../pg_6/login.html';
        throw new Error('Sessão expirada. Faça login novamente.');
    }

    return resposta;
}

// Verifica se o usuário está autenticado
function estaAutenticado() {
    return !!getToken();
}

// Remove o token e os dados do usuário, e redireciona para o login
function fazerLogout() {
    removerToken();
    window.location.href = '../pg_6/login.html';
}

// Atualiza a visibilidade dos itens do menu conforme o estado de autenticação
function atualizarMenu() {
    const autenticado = estaAutenticado();

    const links = {
        nav_perfil: document.getElementById('nav_perfil'),
        nav_sair: document.getElementById('nav_sair'),
        nav_login: document.getElementById('nav_login'),
        nav_cadastro: document.getElementById('nav_cadastro'),
    };

    if (autenticado) {
        // Logado: mostra Perfil e Sair; esconde Login e Criar Conta
        if (links.nav_perfil) links.nav_perfil.style.display = '';
        if (links.nav_sair) links.nav_sair.style.display = '';
        if (links.nav_login) links.nav_login.style.display = 'none';
        if (links.nav_cadastro) links.nav_cadastro.style.display = 'none';

        // Vincula logout ao botão Sair
        const btnSair = document.getElementById('btn_sair');
        if (btnSair) {
            btnSair.onclick = (e) => {
                e.preventDefault();
                fazerLogout();
            };
        }
        const linkSair = document.getElementById('nav_sair');
        if (linkSair) {
            linkSair.onclick = (e) => {
                e.preventDefault();
                fazerLogout();
            };
        }
    } else {
        // Não logado: mostra Login e Criar Conta; esconde Perfil e Sair
        if (links.nav_perfil) links.nav_perfil.style.display = 'none';
        if (links.nav_sair) links.nav_sair.style.display = 'none';
        if (links.nav_login) links.nav_login.style.display = '';
        if (links.nav_cadastro) links.nav_cadastro.style.display = '';
    }
}
