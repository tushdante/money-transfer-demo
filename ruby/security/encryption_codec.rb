require 'openssl'
require 'securerandom'
require 'temporalio/api'
require 'temporalio/converters/payload_codec'
require 'base64'

DEFAULT_KEY = "sa-rocks!sa-rocks!sa-rocks!yeah!"
DEFAULT_KEY_ID = "test"

module Security
  class EncryptionCodec < Temporalio::Converters::PayloadCodec
    def initialize(key_id = DEFAULT_KEY_ID, key = DEFAULT_KEY)
      super()
      @key_id = key_id
      @key = key
    end

    def encode(payloads)
      payloads.map do |p|
        Temporalio::Api::Common::V1::Payload.new(
          metadata: {
            "encoding" => "binary/encrypted".b,
            "encryption-key-id" => @key_id.b
          },
          data: encrypt(p.to_proto)
        )
      end
    end

    def decode(payloads)
      payloads.map do |p|
        encoding = p.metadata["encoding"]
        if encoding != "binary/encrypted"
            p
        else
          key_id = p.metadata["encryption-key-id"]
          unless key_id == @key_id
            raise "Unrecognized key ID #{key_id}. Current key ID is #{@key_id}."
          end
          Temporalio::Api::Common::V1::Payload.decode(decrypt(p.data))
        end
      end
    end

    private

    def encrypt(data)
      cipher = OpenSSL::Cipher.new("aes-256-gcm")
      cipher.encrypt
      cipher.key = @key
      iv = SecureRandom.random_bytes(12)
      cipher.iv = iv

      ciphertext = cipher.update(data) + cipher.final

      iv + ciphertext + cipher.auth_tag
    end

    def decrypt(data)
      begin
        iv = data[0, 12]
        ciphertext = data[12..-17]
        auth_tag = data[-16, 16]

        decipher = OpenSSL::Cipher::AES.new(256, :GCM).decrypt
        decipher.key = @key
        decipher.iv = iv
        decipher.auth_tag = auth_tag
        decipher.update(ciphertext) + decipher.final
      rescue OpenSSL::Cipher::CipherError => e
        # If decryption fails, return the original data
        # This helps with backward compatibility
        puts "Warning: Decryption failed: #{e.message}. Returning original data."
        data
      end
    end
  end
end