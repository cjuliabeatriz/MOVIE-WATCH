const form = document.getElementById('form_login');
const msgErro = document.getElementById('msg_erro');

// Se já estiver logado, redireciona para o perfil
if (estaAutenticado()) {
    window.location.href = '../pg_4/pg4.html';
}

form.addEventListener('submit', async (evento) => {
    evento.preventDefault();

    msgErro.style.display = 'none';

    const email = form.elements.email.value.trim();
    const senha = form.elements.password.value;

    const btn = form.querySelector('button[type="submit"]');
    btn.disabled = true;
    btn.textContent = 'Entrando...';

    try {
        const resposta = await fetch('http://localhost:8081/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                email: email,
                password: senha
            })
        });

        if (!resposta.ok) {
            const erro = await resposta.json();
            mostrarErro(erro.message || 'E-mail ou senha incorretos.');
            return;
        }

        const dados = await resposta.json();

        if (dados.token) {
            salvarToken(dados.token);

            if (dados.usuario) {
                localStorage.setItem('moviewatch_usuario', JSON.stringify(dados.usuario));
            }

            window.location.href = '../pg_4/pg4.html';
        } else {
            mostrarErro('Resposta inválida do servidor.');
        }
    } catch (e) {
        console.error('Erro ao fazer login:', e);
        mostrarErro('Não foi possível conectar ao servidor. Verifique se o backend está rodando.');
    } finally {
        btn.disabled = false;
        btn.textContent = 'Entrar';
    }
});

function mostrarErro(mensagem) {
    msgErro.textContent = mensagem;
    msgErro.style.display = 'flex';
}
