-- Bloque 3, Parte 1 (chat con fotos): permite adjuntar una imagen a un mensaje de chat.
-- El mensaje de texto sigue siendo obligatorio a nivel de aplicacion (se envia vacio o con
-- un placeholder cuando el usuario solo adjunta una imagen); esta columna guarda la URL
-- publica servida por GET /api/chat/imagen/{filename}, mismo patron que fotoPerfilUrl.

ALTER TABLE chat_messages ADD COLUMN IF NOT EXISTS image_url TEXT;
