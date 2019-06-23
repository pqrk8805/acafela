using Acafela.Rpc;
using Grpc.Core;
using harmony.usradmin.rpc;
using System;
using System.Collections.Generic;

namespace AcafelaUserAdmin
{
    public class UserAdminModel
    {
        private UserAdmin.UserAdminClient mClient;

        public UserAdminModel()
        {
            Console.WriteLine("UserAdminModel()");
            Connect();
        }

        private void Connect()
        {
            Channel channel = new Channel(
                                        Config.RPC_SERVER_URL,
                                        ChannelCredentials.Insecure);

            mClient = new UserAdmin.UserAdminClient(channel);
        }

        public IList<harmony.usradmin.rpc.UserInfo> GetUserInfo()
            => mClient.getUserInfoList(new Empty { }).UserInfo;

        public int EnableUser(String email, String phone)
            => mClient.enableUser(
                            new harmony.usradmin.rpc.UserInfo {
                                                    Email = email,
                                                    PhoneNumber = phone}).Err;

        public int DisableUser(String email, String phone)
            => mClient.disableUser(
                            new harmony.usradmin.rpc.UserInfo {
                                                    Email = email,
                                                    PhoneNumber = phone}).Err;

        public int DeleteUser(String email, String phone)
            => mClient.deleteUser(
                            new harmony.usradmin.rpc.UserInfo {
                                                    Email = email,
                                                    PhoneNumber = phone}).Err;
    }
}
