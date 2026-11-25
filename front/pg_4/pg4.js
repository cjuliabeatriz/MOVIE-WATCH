const carregando = document.getElementById('perfil_carregando');
const conteudo = document.getElementById('perfil_conteudo');
const erro = document.getElementById('perfil_erro');
const btnSair = document.getElementById('btn_sair');
const btnEditar = document.getElementById('btn_editar_perfil');
const secaoEdicao = document.getElementById('perfil_edicao');
const btnSalvar = document.getElementById('btn_salvar_edicao');
const btnCancelar = document.getElementById('btn_cancelar_edicao');

const inputNome = document.getElementById('edit_nome');
const inputEmail = document.getElementById('edit_email');
const inputUsername = document.getElementById('edit_username');
const inputFoto = document.getElementById('edit_foto');

let usuarioAtual = null;

async function carregarPerfil() {
    try {
        carregando.style.display = 'block';
        conteudo.style.display = 'none';
        erro.style.display = 'none';
        secaoEdicao.style.display = 'none';

        const resposta = await apiFetch('/users/me');

        if (!resposta.ok) {
            throw new Error('Não autorizado');
        }

        usuarioAtual = await resposta.json();

        document.getElementById('perfil_nome').textContent = usuarioAtual.name || 'Usuário';
        document.getElementById('perfil_email').textContent = usuarioAtual.email || '';
        document.getElementById('perfil_username').textContent = usuarioAtual.username || '@' + (usuarioAtual.email ? usuarioAtual.email.split('@')[0] : 'usuario');
        document.getElementById('perfil_criado').textContent = 'Membro desde ' + formatarData(usuarioAtual.createdAt);

        // Preenche os inputs de edição com os dados atuais
        inputNome.value = usuarioAtual.name || '';
        inputEmail.value = usuarioAtual.email || '';
        inputUsername.value = usuarioAtual.username || '';

        carregando.style.display = 'none';
        conteudo.style.display = 'block';
    } catch (e) {
        console.error('Erro ao carregar perfil:', e);
        carregando.style.display = 'none';
        erro.style.display = 'block';
    }
}

function formatarData(iso) {
    if (!iso) return '';
    const d = new Date(iso);
    return d.toLocaleDateString('pt-BR', {
        day: '2-digit',
        month: 'long',
        year: 'numeric'
    });
}

btnSair.addEventListener('click', (e) => {
    e.preventDefault();
    fazerLogout();
});

btnEditar.addEventListener('click', () => {
    secaoEdicao.style.display = 'block';
    btnEditar.style.display = 'none';
});

btnCancelar.addEventListener('click', () => {
    secaoEdicao.style.display = 'none';
    btnEditar.style.display = 'block';
    inputNome.value = usuarioAtual.name || '';
    inputEmail.value = usuarioAtual.email || '';
    inputUsername.value = usuarioAtual.username || '';
    inputFoto.value = '';
});

btnSalvar.addEventListener('click', async () => {
    btnSalvar.disabled = true;
    btnSalvar.textContent = 'Salvando...';

    try {
        const resposta = await apiFetch('/users/me', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                name: inputNome.value.trim(),
                email: inputEmail.value.trim()
            })
        });

        if (!resposta.ok) {
            const erroDados = await resposta.json();
            throw new Error(erroDados.message || 'Erro ao atualizar perfil.');
        }

        const atualizado = await resposta.json();
        usuarioAtual = atualizado;

        document.getElementById('perfil_nome').textContent = atualizado.name || 'Usuário';
        document.getElementById('perfil_email').textContent = atualizado.email || '';

        secaoEdicao.style.display = 'none';
        btnEditar.style.display = 'block';

        btnSalvar.disabled = false;
        btnSalvar.textContent = 'Salvar alterações';
    } catch (e) {
        console.error('Erro ao salvar perfil:', e);
        alert(e.message || 'Não foi possível salvar as alterações.');
        btnSalvar.disabled = false;
        btnSalvar.textContent = 'Salvar alterações';
    }
});

function tentarNovamente() {
    if (!estaAutenticado()) {
        window.location.href = '../pg_6/login.html';
    } else {
        carregarPerfil();
    }
}

if (!estaAutenticado()) {
    window.location.href = '../pg_6/login.html';
} else {
    carregarPerfil();
}
