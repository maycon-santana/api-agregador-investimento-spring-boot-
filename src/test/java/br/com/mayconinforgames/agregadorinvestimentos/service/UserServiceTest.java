package br.com.mayconinforgames.agregadorinvestimentos.service;

import br.com.mayconinforgames.agregadorinvestimentos.entity.User;
import br.com.mayconinforgames.agregadorinvestimentos.entity.dto.user.CreateUserDto;
import br.com.mayconinforgames.agregadorinvestimentos.entity.dto.user.UpdateUserDto;
import br.com.mayconinforgames.agregadorinvestimentos.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    @Captor
    private ArgumentCaptor<UUID> uuidArgumentCaptor;

    @Nested
    class createUser {

        @Test
        @DisplayName("Deve cadastrar um usuário com sucesso")
        void deveCadastraUmUsuarioComSucesso() {

            var user = new User(
                    UUID.randomUUID(),
                    "username",
                    "email@email.com",
                    "1234",
                    Instant.now(),
                    null
            );
            doReturn(user).when(userRepository).save(userArgumentCaptor.capture());
            var input = new CreateUserDto(
                    "username",
                    "email@email.com",
                    "1234"
            );

            var output = userService.createUser(input);

            assertNotNull(output);

            var userCaptured = userArgumentCaptor.getValue();

            assertEquals(input.username(), userCaptured.getUsername());
            assertEquals(input.email(), userCaptured.getEmail());
            assertEquals(input.password(), userCaptured.getPassword());

        }

        @Test
        @DisplayName("Deve mostrar uma exceção se houver algum erro")
        void deveJogarUmaExcessaoQuandoAlgumErroAcontecer() {

            doThrow(new RuntimeException()).when(userRepository).save(any());
            var input = new CreateUserDto(
                    "username",
                    "email@email.com",
                    "1234"
            );

            assertThrows(RuntimeException.class, () -> userService.createUser(input));

        }

    }

    @Nested
    class getUserById {

        @Test
        @DisplayName("Deve obter o usuário por ID com sucesso quando opcional estiver presente")
        void deveRetornarUsuarioPorIDQuandoOptionalEstiverPresente() {

            var user = new User(
                    UUID.randomUUID(),
                    "username",
                    "email@email.com",
                    "1234",
                    Instant.now(),
                    null
            );
            doReturn(Optional.of(user))
                    .when(userRepository)
                    .findById(uuidArgumentCaptor.capture());

            var output = userService.getUserById(user.getUserId().toString());

            assertTrue(output.isPresent());
            assertEquals(user.getUserId(), uuidArgumentCaptor.getValue());

        }

        @Test
        @DisplayName("Deve obter o usuário por ID com sucesso quando opcional estiver vazio")
        void deveRetornarUsuarioPorIDQuandoOptionalEstiverVazio() {

            var userId = UUID.randomUUID();
            doReturn(Optional.empty())
                    .when(userRepository)
                    .findById(uuidArgumentCaptor.capture());

            var output = userService.getUserById(userId.toString());

            assertTrue(output.isEmpty());
            assertEquals(userId, uuidArgumentCaptor.getValue());

        }

    }

    @Nested
    class listUsers {

        @Test
        @DisplayName("Deve retornar uma lista de usuários")
        void deveRetornarUmListaDeUsuarios() {

            var user = new User(
                    UUID.randomUUID(),
                    "username",
                    "email@email.com",
                    "1234",
                    Instant.now(),
                    null
            );
            var userList = List.of(user);
            doReturn(userList)
                    .when(userRepository)
                    .findAll();

            var output = userService.listUsers();

            assertNotNull(output);
            assertEquals(userList.size(), output.size());

        }

    }

    @Nested
    class deleteById {

        @Test
        @DisplayName("Deve excluir um usuário com sucesso se ele existe")
        void deveExcluirUmUsuarioComSucessoExiste() {

            doReturn(true)
                    .when(userRepository)
                            .existsById(uuidArgumentCaptor.capture());

            doNothing()
                    .when(userRepository)
                    .deleteById(uuidArgumentCaptor.capture());

            var userId = UUID.randomUUID();

            userService.deleteById(userId.toString());

            var idList = uuidArgumentCaptor.getAllValues();
            assertEquals(userId, idList.get(0));
            assertEquals(userId, idList.get(1));

            // verifica se o metodo by está sendo chamado
            verify(userRepository, times(1)).existsById(idList.get(0));
            verify(userRepository, times(1)).existsById(idList.get(1));

        }

        @Test
        @DisplayName("Não deve excluir o usuário quando o usuário não existe")
        void naoDeveExcluirUmUsuarioNaoExiste() {

            doReturn(false)
                    .when(userRepository)
                    .existsById(uuidArgumentCaptor.capture());
            var userId = UUID.randomUUID();

            userService.deleteById(userId.toString());

            assertEquals(userId, uuidArgumentCaptor.getValue());

            // verifica se o metodo by está sendo chamado
            verify(userRepository, times(1))
                    .existsById(uuidArgumentCaptor.getValue());

            verify(userRepository, times(0)).deleteById(any());

        }

    }

    @Nested
    class updateUserById {

        @Test
        @DisplayName("\n" +
                "Deve atualizar o usuário por id quando o usuário existir e o nome de usuário e a senha forem preenchidos")
        void deveAtualizarUmUsuarioComSucessoExisteNomeSenhaPreenchidos() {

            var updateUserDto = new UpdateUserDto(
                      "Novo usuario",
                    "novasenha"
            );
            var user = new User(
                    UUID.randomUUID(),
                    "username",
                    "email@email.com",
                    "1234",
                    Instant.now(),
                    null
            );
            doReturn(Optional.of(user))
                    .when(userRepository)
                    .findById(uuidArgumentCaptor.capture());

            doReturn(user)
                    .when(userRepository)
                    .save(userArgumentCaptor.capture());

            userService.updateUserById(user.getUserId().toString(), updateUserDto);

            assertEquals(user.getUserId(), uuidArgumentCaptor.getValue());

            var userCaptured = userArgumentCaptor.getValue();

            assertEquals(updateUserDto.username(), userCaptured.getUsername());
            assertEquals(updateUserDto.password(), userCaptured.getPassword());

            verify(userRepository, times(1))
                    .findById(uuidArgumentCaptor.getValue());

            verify(userRepository, times(1))
                    .save(user);

        }

        @Test
        @DisplayName("\n" +
                "Não deve atualizar o usuário se o usuário não existi")
        void naoDeveAtualizarUmUsuarioSeUsuarioNaoExiste() {

            var updateUserDto = new UpdateUserDto(
                    "Novo usuario",
                    "novasenha"
            );

            var userId = UUID.randomUUID();
            doReturn(Optional.empty())
                    .when(userRepository)
                    .findById(uuidArgumentCaptor.capture());

            userService.updateUserById(userId.toString(), updateUserDto);

            assertEquals(userId, uuidArgumentCaptor.getValue());

            verify(userRepository, times(1))
                    .findById(uuidArgumentCaptor.getValue());

            verify(userRepository, times(0))
                    .save(any());

        }

    }
}